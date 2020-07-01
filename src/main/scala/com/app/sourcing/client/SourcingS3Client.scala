package com.app.sourcing.client


import cats.effect.IO
import com.amazonaws.services.s3.model.{Bucket, CreateBucketRequest, PutObjectResult}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}


case class S3Object(objects: List[Option[Any]], bucketName: String, fileName: String)

case class SourcingS3ClientRequest(request: S3Object)

object SourcingS3Client {

  def apply(): SourcingS3Client = {
    val s3Client: AmazonS3 = AmazonS3Client.builder().build()
    new SourcingS3Client(s3Client)
  }
}

class SourcingS3Client(s3Client: AmazonS3) extends Client {


  def saveFileCSV(dataToSave: List[SourcingS3ClientRequest]): IO[List[PutObjectResult]] = {
    import cats.implicits._
    val finalResult = dataToSave.map {
      data =>
        for {
          bucket <- createBucket(data.request.bucketName)
          result <- uploadFileString(data.request.objects, bucket, data.request.fileName)
        } yield (result)
    }
    finalResult.sequence
  }

  private def createBucket(bucketName: String): IO[Bucket] = {
    import scala.collection.JavaConverters._
    IO {
      if (!s3Client.doesBucketExistV2(bucketName)) {
        s3Client.createBucket(new CreateBucketRequest(bucketName))
      } else {
        val buckets = s3Client.listBuckets().asScala
        buckets reduce {
          (a, b) =>
            if (b.getName.equals(bucketName)) {
              b
            } else {
              a
            }
        }
      }
    }
  }

  private def uploadFileString(users: List[Option[Any]], bucket: Bucket, name: String) = {
    IO {
      val content = users.filter(user => user.isDefined).map(user => user.get).mkString("\n")
      s3Client.putObject(bucket.getName, name, content)
    }
  }


}
