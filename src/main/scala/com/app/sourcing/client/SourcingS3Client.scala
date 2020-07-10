package com.app.sourcing.client

import cats.effect.IO
import com.amazonaws.services.s3.model.{Bucket, CreateBucketRequest, PutObjectResult}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}

final case class S3Object(objects: List[Option[Any]], bucketName: String, fileName: String)

final case class SourcingS3ClientRequest(request: S3Object)

object SourcingS3Client {

  def apply(region: String): SourcingS3Client = {
    val s3Client: AmazonS3 = AmazonS3Client.builder().withRegion(region).build()
    new SourcingS3Client(s3Client)
  }
}

class SourcingS3Client(s3Client: AmazonS3) extends S3Client {

  def saveFileCSV(dataToSave: List[SourcingS3ClientRequest]): IO[List[PutObjectResult]] = {
    import cats.implicits._
    val finalResult = dataToSave.map { data =>
      for {
        bucket <- createBucket(data.request.bucketName)
        result <- uploadFileString(data.request.objects, bucket, data.request.fileName)
      } yield result
    }
    finalResult.sequence
  }

  private def createBucket(bucketName: String): IO[Bucket] = {
    import scala.collection.JavaConverters._
    IO {
      if (s3Client.doesBucketExistV2(bucketName)) {
        val buckets = s3Client.listBuckets().asScala
        buckets reduce { (a, b) =>
          if (b.getName.equals(bucketName)) b else a
        }
      } else {
        s3Client.createBucket(new CreateBucketRequest(bucketName))
      }
    }
  }

  private def uploadFileString(users: List[Option[Any]], bucket: Bucket, name: String) = {
    IO {
      val content = users.filter(_.isDefined).map(_.get).mkString("\n")
      s3Client.putObject(bucket.getName, name, content)
    }
  }

}
