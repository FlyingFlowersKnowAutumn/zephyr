# 文章计数对账脚本
#
# 用途：把 articles 表上冗余的 comment_count / like_count 按明细表重新算一遍。
# 特性：可重复执行（幂等），随时跑都安全；只改计数字段，不动正文与明细。
#
# 注意：文件必须保存为 UTF-8 with BOM，否则 Windows PowerShell 5.1 会按 GBK 解析、中文报错。
# 用法：
#   powershell -NoProfile -ExecutionPolicy Bypass -File scripts/reconcile-counts.ps1          # 修复
#   powershell -NoProfile -ExecutionPolicy Bypass -File scripts/reconcile-counts.ps1 -DryRun  # 只报告，不改数据

param(
    [string]$Container = "blog-postgres",
    [string]$DbUser = "postgres",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Psql([string]$db, [string]$sql) {
    $out = docker exec $Container psql -U $DbUser -d $db -t -A -F'|' -c $sql 2>&1
    if ($LASTEXITCODE -ne 0) { throw "psql 执行失败（$db）：$out" }
    return $out
}

Write-Host "== 1/4 读取文章声明值 ==" -ForegroundColor Cyan
$articleRows = @()
foreach ($line in (Invoke-Psql "article_db" "SELECT id::text, slug, comment_count, like_count FROM articles")) {
    if ($line -match '^\s*$') { continue }
    $p = $line -split '\|'
    $articleRows += [pscustomobject]@{
        Id  = $p[0].Trim()
        Slug = $p[1]
        DeclaredComments = [int]$p[2]
        DeclaredLikes    = [int]$p[3]
    }
}
Write-Host ("共 {0} 篇文章" -f $articleRows.Count)

Write-Host "== 2/4 统计明细真实值 ==" -ForegroundColor Cyan
$comments = @{}
foreach ($line in (Invoke-Psql "interaction_db" "SELECT article_id::text, count(*) FROM comments GROUP BY article_id")) {
    if ($line -match '^([0-9a-f-]{36})\|(\d+)\s*$') { $comments[$Matches[1]] = [int]$Matches[2] }
}
$likes = @{}
foreach ($line in (Invoke-Psql "interaction_db" "SELECT article_id::text, count(*) FROM reactions GROUP BY article_id")) {
    if ($line -match '^([0-9a-f-]{36})\|(\d+)\s*$') { $likes[$Matches[1]] = [int]$Matches[2] }
}
Write-Host ("有评论的文章 {0} 篇，有点赞的文章 {1} 篇" -f $comments.Count, $likes.Count)

Write-Host "== 3/4 比对 ==" -ForegroundColor Cyan
$drift = @()
foreach ($a in $articleRows) {
    $realC = if ($comments.ContainsKey($a.Id)) { $comments[$a.Id] } else { 0 }
    $realL = if ($likes.ContainsKey($a.Id)) { $likes[$a.Id] } else { 0 }
    if ($a.DeclaredComments -ne $realC -or $a.DeclaredLikes -ne $realL) {
        $drift += [pscustomobject]@{
            Slug = $a.Slug
            Id = $a.Id
            CommentFrom = $a.DeclaredComments; CommentTo = $realC
            LikeFrom = $a.DeclaredLikes;       LikeTo = $realL
        }
    }
}

if ($drift.Count -eq 0) {
    Write-Host "没有漂移，数据一致。" -ForegroundColor Green
    exit 0
}

$drift | Select-Object Slug,
    @{n='评论';e={"$($_.CommentFrom) → $($_.CommentTo)"}},
    @{n='点赞';e={"$($_.LikeFrom) → $($_.LikeTo)"}} | Format-Table -AutoSize

if ($DryRun) {
    Write-Host ("-DryRun：检测到 {0} 篇漂移，未做任何修改。" -f $drift.Count) -ForegroundColor Yellow
    exit 0
}

Write-Host "== 4/4 回写 article_db ==" -ForegroundColor Cyan
$values = ($drift | ForEach-Object { "('$($_.Id)'::uuid, $($_.CommentTo), $($_.LikeTo))" }) -join ","
$sql = @"
UPDATE articles AS a
SET comment_count = v.cc, like_count = v.lc
FROM (VALUES $values) AS v(id, cc, lc)
WHERE a.id = v.id;
"@
Invoke-Psql "article_db" $sql | Out-Null

# 复核
$after = @{}
foreach ($line in (Invoke-Psql "article_db" "SELECT id::text, comment_count, like_count FROM articles")) {
    if ($line -match '^([0-9a-f-]{36})\|(\d+)\|(\d+)\s*$') { $after[$Matches[1]] = "$($Matches[2])|$($Matches[3])" }
}
$still = 0
foreach ($a in $articleRows) {
    $realC = if ($comments.ContainsKey($a.Id)) { $comments[$a.Id] } else { 0 }
    $realL = if ($likes.ContainsKey($a.Id)) { $likes[$a.Id] } else { 0 }
    if ($after[$a.Id] -ne "$realC|$realL") { $still++ }
}
if ($still -eq 0) {
    Write-Host ("修复完成，{0} 篇已更新，复核无漂移。" -f $drift.Count) -ForegroundColor Green
} else {
    Write-Host ("修复后仍有 {0} 篇不一致，请检查。" -f $still) -ForegroundColor Red
    exit 1
}
