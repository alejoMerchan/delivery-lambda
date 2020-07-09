package com.app.sourcing.service.conf

trait BucketConf extends ConfigLoad {
  def bucketName: String
  def regionName: String

}

object BucketConf extends BucketConf {
  def bucketName: String = config.getString("bucket.bucket-name")
  def regionName: String = config.getString("bucket.region-name")
}