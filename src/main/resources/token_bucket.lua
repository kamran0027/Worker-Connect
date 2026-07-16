local bucket = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])

local now = redis.call("TIME")
local current = now[1]

local tokensKey = bucket..":tokens"
local timestampKey = bucket..":ts"

local tokens = tonumber(redis.call("GET", tokensKey))

if tokens == nil then
    tokens = capacity
end

local last = tonumber(redis.call("GET", timestampKey))

if last == nil then
    last = current
end

local delta = math.max(0, current-last)

tokens = math.min(capacity,
tokens + delta * refillRate)

local allowed = 0

if tokens >= 1 then

    tokens = tokens - 1
    allowed = 1

end

redis.call("SET", tokensKey, tokens)
redis.call("SET", timestampKey, current)

redis.call("EXPIRE", tokensKey, 3600)
redis.call("EXPIRE", timestampKey, 3600)

return allowed