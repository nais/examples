FROM golang:1.22-alpine as builder
RUN apk add --no-cache git make curl build-base
ENV GOOS=linux
WORKDIR /src
COPY . /src/
RUN go mod download
RUN go build -o bin/flaky-service *.go

FROM alpine:3.18
RUN apk add --no-cache ca-certificates tzdata
RUN export PATH=$PATH:/app
WORKDIR /app
COPY --from=builder /src/bin/flaky-service /app/flaky-service
CMD ["/app/flaky-service"]