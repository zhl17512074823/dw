package com.dgmall.dw

import scala.util.Random
import scala.collection.mutable

object RandomNumUtil {

  //随机数生成器对象
  private val random = new Random()

  /**
    * 生成随机的整数，区间 from，to
    *
    * @param from
    * @param to
    */
  def randomInt(from: Int, to: Int)= {
    if (from > to) throw new IllegalArgumentException(s"from:$from 不能大于 to: $to")
    else random.nextInt(to - from + 1) + from
  }

  /**
    * 创建多个 Int 值
    *
    * @param from
    * @param to
    * @param count     创建的 Int 值的顺序
    * @param canRepeat 是否允许重复
    * @return List[Int] 集合
    */
  def randomMultiInt(from:Int,to:Int,count:Int,canRepeat:Boolean=true): List[Int] ={
    if(canRepeat){
      (1 to count).toList.map(_=> randomInt(from,to))
    }else{
      val set = mutable.Set[Int]()
      while(set.size<count){
        set+= randomInt(from,to)
      }
      set.toList
    }
  }
  
  def randomLong(from:Long,to:Long)={
    if (from>to)throw new IllegalArgumentException(s"from:$from 不能大于 to: $to ")
    else math.abs(random.nextLong())%(to-from + 1) + from
  }


}
