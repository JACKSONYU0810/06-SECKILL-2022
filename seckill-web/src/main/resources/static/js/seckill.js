
//定义一个对象secKillObj,里面的属性url和fun也是对象
//url 对象中，拥有若干个方法，用于返回请求地址路径，可以实现请求路径重用的特点
//fun 对象中，拥有若干个方法，用于实现秒杀的具体业务逻辑控制
var secKillObj = {
    url: {
        getSysTime: function () {
            //返回请求地址,实现地址的复用
            return "/getSysTime"
        },
        getRandomName:function (goodsId) {
            return "/getRandomName/"+goodsId
        },
        secKill:function (goodsId, randomName) {
            return "/secKill/"+goodsId+"/"+randomName
        },
        getOrdersResult:function (goodsId) {
            return "/getOrdersResult/"+goodsId
        }

    },
    fun: {
        initSecKill: function (goodsId, startTime, endTime) {
            $.ajax({
                url: secKillObj.url.getSysTime(),
                type: "get",
                dataType: "json",
                success: function (data) {
                    if (data.result<startTime){
                        //活动未开始,调用倒计时方法
                        secKillObj.fun.secKillCountdown(goodsId,startTime*1)
                        return false
                    }
                    if (data.result>endTime){
                        $("#secKillButSpan").html('<span style="color: red">秒杀活动已经结束</span>')
                        return false
                    }
                    secKillObj.fun.doSecKill(goodsId)
                },
                error: function () {
                    alert("服务器繁忙,请稍后再试111!")
                }
            })
        },
        secKillCountdown:function (goodsId, startTime) {
            //根据开始时间,定义倒计时的结束时间
            var targetTime = new Date(startTime*1)
            //使用任意一个jQuery的对象来调用countdown方法实现倒计时
            //参数 1 为倒计时的目标时间
            //参数 2 为倒计时的回调方法，这个方法每秒钟被自动调用一次，用于更新页面的效果
            $("#secKillButSpan").countdown(targetTime,function(event){
                var v_context=event.strftime('<span style="color: red">距离活动开始还有：%D天 %H小时 %M分钟 %S秒</span>')
                $("#secKillButSpan").html(v_context)
            }).on("finish.countdown",function(){
                //倒计时结束以后的回调方法用于显示秒杀按钮到页面中
                //这里有2种写法
                // 1、刷新当前页面，不推荐使用可能会引发高并发的现象
                // 2、调用某个函数来更新页面的效果 推荐的
                secKillObj.fun.doSecKill(goodsId)
            })
        },
        doSecKill:function (goodsId) {
            $("#secKillButSpan").html('<input type="button" value="立即抢购" id="secKillBut">\n')
            //为秒杀按钮绑定单击事件
            $("#secKillBut").on("click",function () {

                //设置禁用秒杀按钮,防止用户对此购买,但是刷新页面还是可以购买
                $(this).attr("disabled",true)

                //开始秒杀,获取随机名称
                $.ajax({
                    url: secKillObj.url.getRandomName(goodsId),
                    type: "get",
                    dataType: "json",
                    success: function (data) {
                        if (data.code != "0") {
                            alert(data.message)
                            return false
                        }
                        //执行秒杀操作
                        secKillObj.fun.secKill(goodsId,data.result)
                    },
                    error: function () {
                        alert("服务器繁忙,请稍后再试222!")
                    }
                })
            })
        },
        secKill:function (goodsId,randomName) {
            $.ajax({
                url: secKillObj.url.secKill(goodsId,randomName),
                type: "get",
                dataType: "json",
                success: function (data) {
                    if (data.code != "0") {
                        alert(data.message)
                        return false
                    }
                    //返回成功,表示完成秒杀功能,并将订单存入到redis
                    //接下来就是从redis中获取订单,进行支付
                    secKillObj.fun.getOrdersResult(goodsId)
                },
                error: function () {
                    alert("服务器繁忙,请稍后再试333!")
                }
            })
        },
        getOrdersResult:function (goodsId) {
            $.ajax({
                url: secKillObj.url.getOrdersResult(goodsId),
                type: "get",
                dataType: "json",
                success: function (data) {
                    if (data.code != "0") {
                        //如果获取订单失败(redis中还没有存入订单),就设置每隔3秒访问一次
                        window.setTimeout("secKillObj.fun.getOrderResult("+goodsId+")",3000)
                        return false
                    }
                    var orderMoney = data.result.orderMoney
                    var orderId = data.result.id
                    //如果获取订单成功,显示订单,并提示进行支付
                    $("#secKillButSpan").html('<span style="color: red">下单成功: 共计 '+orderMoney+' 元 <a href="/pay/'+orderId+'">立即支付</a></span>')

                },
                error: function () {
                    alert("服务器繁忙,请稍后再试444!")
                }
            })
        }
    }
}