--参数列表
local voucherId = ARGV[1]
local userId = ARGV[2]
--拼接key
local orderKey = 'seckill:order:' .. voucherId
local stockKey = 'seckill:stock:' .. voucherId

if (tonumber(redis.call('get',stockKey)) <= 0) then
    --库存不足
    return 1
end
if (redis.call('sismember',orderKey,userId) == 1) then
    --用户已经下过单
    return 2
end

redis.call('decrby',stockKey,1)
redis.call('sadd',orderKey,userId)

return 0