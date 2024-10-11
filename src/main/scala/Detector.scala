package com.thecodeshewrites.frauddetector

// dummy change!

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._

// object creates a singleton (diff from class) => holds static val/var and methods
// there are others uses (ex companion object)
object Detector {
  def main(args: Array[String]) {
    val spark = SparkSession.builder
      .master("local")
      .appName("Fraud Detector")
      .config("spark.driver.memory", "2g")
      .enableHiveSupport
      .getOrCreate()

    import spark.implicits._
    val financesDF = spark.read.json("Data/finances-small.json")
    financesDF
      .na.drop("all", Seq("ID","Account","Amount","Description","Date")) // drop rows were ALL columns (from the selection) are null or NaN
      .na.fill("Unknown", Seq("Description")) // replace null values selected columns
      .where($"Amount" =!= 0 || $"Description" === "Unknown")
      .selectExpr("Account.Number as AccountNumber", "Amount", "Date", "Description")
      .write.mode(SaveMode.Overwrite).parquet("Output/finances-small")

    if(financesDF.hasColumn("_corrupt_record")) {
      financesDF.where($"_corrupt_record".isNotNull).select($"_corrupt_record")
        .write.mode(SaveMode.Overwrite).text("Output/corrupt_finances")
    }

    financesDF
      // concat word separator => first argument = separator
      .select(concat_ws(" ", $"Account.FirstName", $"Account.LastName").as("FullName"),
      //.select(concat($"Account.FirstName", lit(" "), $"Account.LastName").as("FullName"),
        $"Account.Number".as("AccountNumber"))
      // dropDuplicates can also take columns as parameter
      // dropDuplicates and distinct don't work the same with streaming data!
      //.dropDuplicates()
      .distinct
      .coalesce(5)
      .write.mode(SaveMode.Overwrite).json("Output/finances-small-accounts")

    financesDF
      .select($"Account.Number".as("AccountNumber"), $"Amount", $"Description",
        to_date(unix_timestamp($"Date","MM/dd/yyyy").cast("timestamp")).as("Date"))
      .groupBy($"AccountNumber")
      .agg(avg($"Amount").as("AverageTransaction"), sum($"Amount").as("TotalTransactions"),
        count($"Amount").as("NumberOfTransactions"), max($"Amount").as("MaxTransaction"),
        min($"Amount").as("MinTransaction"), stddev($"Amount").as("StandardDeviationAmount"),
        collect_set($"Description").as("UniqueTransactionDescriptions"))
      .coalesce(5)
      .write.mode(SaveMode.Overwrite).json("Output/finances-small-account-details")

  }

  implicit class DataFrameHelper(df: DataFrame) {
    import scala.util.Try
    def hasColumn(columnName: String) = Try(df(columnName)).isSuccess
  }
}
