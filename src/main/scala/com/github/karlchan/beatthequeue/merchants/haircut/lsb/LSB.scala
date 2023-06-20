package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._

final class LSB extends Merchant[LSB, LSBCriteria, LSBEvent]:
  override val name = LSB.Name
  override val logoUrl =
    "data:image/svg+xml,%3Csvg version='1.1' id='Layer_1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' viewBox='0 0 172 202' style='enable-background:new 0 0 172 202;' xml:space='preserve'%3E%3Cstyle type='text/css'%3E.st0%7Bfill:%23E7441C%7D.st1%7Bfill:%23FD3D00%7D.st2%7Bfill:%231C1835%7D.st3%7Bfill:%23FFF%7D%3C/style%3E%3Cg%3E%3Cpath class='st0' d='M27,17.7'%3E%3C/path%3E%3Cpath class='st1' d='M41.9,40.1c10.9,15.1,21,28,25.1,30.5c9.1,5.6,16.1,6.2,18.4,6.2h0.8c0.2,0,4.4,10.2,10.1,24.4%0Ac12.5,30.8,32.2,80,32.8,81.6l-43.6-65.4l-10.8-16.2L36.2,43.2 M85.5,105.6c1.8,0,3.2-1.4,3.2-3.2c0-1.8-1.4-3.2-3.2-3.2%0Ac-1.8,0-3.2,1.4-3.2,3.2C82.3,104.2,83.8,105.6,85.5,105.6'%3E%3C/path%3E%3Cpath class='st1' d='M36.7,43.2c-2.3,0.8-4.8,1.3-7.5,1.3c-11.9,0-21.6-9.7-21.6-21.6C7.6,11,17.3,1.3,29.3,1.3S50.9,11,50.9,22.9%0Ac0,7-3.3,13.2-8.5,17.2 M29.3,39.1c9,0,16.2-7.3,16.2-16.2c0-9-7.3-16.2-16.2-16.2S13.1,14,13.1,22.9%0AC13.1,31.9,20.3,39.1,29.3,39.1'%3E%3C/path%3E%3Cpath class='st2' d='M143.8,17.7'%3E%3C/path%3E%3Cpath class='st2' d='M129.2,40.1c-10.9,15.1-21,28-25.1,30.5c-9.1,5.6-16.1,6.2-18.4,6.2h-0.8c-0.2,0-4.4,10.2-10.1,24.4%0Ac-12.5,30.8-32.2,80-32.8,81.6l43.6-65.4l10.8-16.2l38.6-57.9 M85.5,105.6c-1.8,0-3.2-1.4-3.2-3.2c0-1.8,1.4-3.2,3.2-3.2%0Ac1.8,0,3.2,1.4,3.2,3.2C88.8,104.2,87.3,105.6,85.5,105.6'%3E%3C/path%3E%3Cpath class='st2' d='M134.4,43.2c2.3,0.8,4.8,1.3,7.5,1.3c11.9,0,21.6-9.7,21.6-21.6c0-11.9-9.7-21.6-21.6-21.6%0As-21.6,9.7-21.6,21.6c0,7,3.3,13.2,8.5,17.2 M141.9,39.1c-9,0-16.2-7.3-16.2-16.2c0-9,7.3-16.2,16.2-16.2s16.2,7.3,16.2,16.2%0AC158.1,31.9,150.8,39.1,141.9,39.1'%3E%3C/path%3E%3Cpolygon class='st2' points='4.2,94.9 1,94.9 1,117.6 14.3,117.6 14.3,114.5 4.2,114.5 '%3E%3C/polygon%3E%3Cpath class='st2' d='M156,114.5c2,2.4,4.8,3.8,7.9,3.8c4,0,7-2.6,7-6.6c0-2.6-1.4-4.2-2.9-5.2c-2.9-1.9-7.7-2.6-7.7-5.4%0Ac0-2,1.9-3,3.8-3c1.4,0,2.8,0.5,4,1.6l1.9-2.4c-1.2-1-3.2-2.3-6.1-2.3c-4,0-6.9,2.7-6.9,6c0,2.4,1.3,3.9,2.9,5c3,2,7.8,2.4,7.8,5.8%0Ac0,2-1.8,3.4-4,3.4c-2.3,0-4.2-1.3-5.5-2.9L156,114.5z'%3E%3C/path%3E%3Cpath class='st2' d='M87,200.7c2.3,0,3.8-0.6,5-1.5c1.5-1.2,2.5-3.1,2.5-5.2c0-2.6-1.6-4.9-3.7-6c1.1-1,1.8-2.5,1.8-4%0Ac0-1.7-0.7-3.2-1.9-4.3c-1.1-1-2.5-1.6-4.7-1.6h-7.7v22.7H87z M81.4,187v-5.8h3.9c1.8,0,2.6,0.4,3.1,0.9c0.5,0.5,0.8,1.2,0.8,2%0Ac0,0.8-0.3,1.5-0.8,2c-0.5,0.6-1.3,0.9-3.1,0.9H81.4z M81.4,197.7V190h4.9c2,0,3,0.5,3.7,1.2c0.6,0.7,1.1,1.6,1.1,2.6%0Ac0,1-0.4,2-1.1,2.7c-0.7,0.7-1.7,1.2-3.7,1.2H81.4z'%3E%3C/path%3E%3Ccircle class='st3' cx='85.5' cy='102.4' r='3.3'%3E%3C/circle%3E%3C/g%3E%3C/svg%3E"
  override val eventFinder = LSBCrawler()
  override val criteriaFactory = () => LSBCriteria()
  override val renderer = LSBRenderer()

object LSB:
  val Name = "London School of Barbering"
