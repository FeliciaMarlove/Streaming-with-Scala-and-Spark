# Use a Scala sbt image https://github.com/sbt/docker-sbt
# <jdk version>_<sbt version>_<scala version>
FROM hseeberger/scala-sbt:graalvm-ce-21.1.0-java8_1.5.5_2.12.15
# Set the working directory to /app
WORKDIR /app
# Copy content into the container at /app
COPY ./src/main/scala/. /app/src/main/scala/.
COPY ./build.sbt /app
COPY ./Data/. /app/Data
# Update and install dependencies from build.sbt
RUN sbt update
# Compile the Scala code
RUN sbt compile
# Run the Scala program
CMD ["sbt", "run"]


# Build the Docker image
# docker build -t <image_name> <path_to_dockerfile_directory>
# ex:  docker build -t docker-scala-img .

# List all Docker images
# docker images

# Run an image
# docker run <image_name>

# Write to host (todo)
# docker run -v $(pwd)/src:/src scala-detector-img
