# How to - Read parquet files

```
pip install parquet-tools
parquet-tools show ./Output/FOLDER/FILE_PATH.parquet 
```

# Course
[Handling Fast Data with Apache Spark SQL and Streaming (Pluralsight)](https://app.pluralsight.com/library/courses/apache-spark-sql-fast-data-handling-streaming/exercise-files)

# Notes

/!\ order of operations matters.
This will run, but it'll limit to 5 random rows before ordering

```financesDf.limit(5).orderBy("Amount").show```

This will order, then limit

```financesDf.orderBy("Amount").limit(5).show```
     