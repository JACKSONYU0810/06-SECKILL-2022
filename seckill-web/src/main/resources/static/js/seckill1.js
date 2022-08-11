//定义一个对象secKillObj,里面的属性url和fun也是对象
//url 对象中，拥有若干个方法，用于返回请求地址路径，可以实现请求路径重用的特点
//fun 对象中，拥有若干个方法，用于实现秒杀的具体业务逻辑控制
var seckillObj = {
    url: {
        getSysTime: function () {
            return "/getSysTime"
        },

        getRandomName: function (goodsId) {
            return "/getRandomName/" + goodsId
        },

        secKill: function (goodsId, randomName) {
            return "/secKill/" + goodsId + "/" + randomName
        },

        getOrderResult:function (goodsId) {
            return "/getOrderResult/"+goodsId
        }

    },
    fun: {

        initSeckill: function (goodsId, startTime, endTime) {
            $.ajax({
                url: seckillObj.url.getSysTime(),
                type: "get",
                dataType: "json",
                success: function (data) {
                    if (data.result < startTime) {
                        seckillObj.fun.secKillCountDown(goodsId, startTime)
                    } else if (data.result > endTime) {
                        $("#secKillButSpan").html('<span style="color: red">秒杀活动已经结束</span>')
                        return false
                    } else {
                        seckillObj.fun.doSecKill(goodsId)
                    }
                },
                error: function (data) {
                    alert("服务繁忙，请稍后再试111")
                }
            })
        },

        secKillCountDown: function (goodsId, startTime) {
            //根据开始时间,定义倒计时的结束时间
            var targetTime = new Date(startTime)
            //使用任意一个jQuery的对象来调用countdown方法实现倒计时
            //参数 1 为倒计时的目标时间
            //参数 2 为倒计时的回调方法，这个方法每秒钟被自动调用一次，用于更新页面的效果
            $("#secKillButSpan").countdown(targetTime, function (event) {
                var v_context = event.strftime('<span style="color: red">距离活动开始还有：%D天 %H小时 %M分钟 %S秒</span>')
                $("#secKillButSpan").html(v_context)
            }).on("finish.countdown", function () {
                //倒计时结束以后的回调方法用于显示秒杀按钮到页面中
                //这里有2种写法
                // 1、刷新当前页面，不推荐使用可能会引发高并发的现象
                // 2、调用某个函数来更新页面的效果 推荐的
                seckillObj.fun.doSecKill(goodsId)
            })
        },

        doSecKill: function (goodsId) {
            $("#secKillButSpan").html('<input type="button" value="立即抢购" id="secKillBut"/>')

            $("#secKillBut").on("click", function () {
                $("#secKillBut").attr("disabled", true)
                $.ajax({
                    url: seckillObj.url.getRandomName(goodsId),
                    type: "get",
                    dataType: "json",
                    success: function (data) {
                        if (data.code != "0") {
                            alert(data.message)
                            return false
                        }
                        seckillObj.fun.secKill(goodsId, data.result)
                    },
                    error: function (data) {
                        alert("服务繁忙，请稍后再试222")
                    }
                })
            })
        },

        //秒杀开启之前应该通过定时任务将商品库存存放到redis中
        secKill: function (goodsId, randomName) {
            $.ajax({
                url: seckillObj.url.secKill(goodsId, randomName),
                type: "get",
                dataType: "json",
                success: function (data) {
                    if (data.code != "0") {
                        alert(data.message)
                        return false
                    }
                    //返回成功表示秒杀成功，并将订单保存在数据库中，接下来就是获取订单
                    seckillObj.fun.getOrderResult(goodsId)
                },
                error: function (data) {
                    alert("服务繁忙，请稍后再试333")
                }
            })
        },

        //获取订单信息，完成支付功能
        getOrderResult:function (goodsId) {
            $.ajax({
                url: seckillObj.url.getOrderResult(goodsId),
                type: "get",
                dataType: "json",
                success: function (data) {
                    //进入if表示，没有获取到订单成功信息，可能是请求太多导致没有下单完成，因此要递归调用获取订单信息
                    if (data.code != "0") {
                        window.setTimeout("seckillObj.url.getOrderResult("+goodsId+")",3000)
                        return false
                    }
                    //获取订单信息成功，进行支付功能
                    var orderMoney = data.result.orderMoney
                    var orderId = data.result.id
                    //如果获取订单成功,显示订单,并提示进行支付
                    $("#secKillButSpan").html('<span style="color: red">下单成功: 共计 '+orderMoney+' 元 <a href="/pay/'+orderId+'">立即支付</a></span>')

                },
                error: function (data) {
                    alert("服务繁忙，请稍后再试333")
                }
            })
        }


    }
}