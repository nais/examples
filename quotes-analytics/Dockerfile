# Build stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copy solution and project files
COPY quotes-analytics.sln .
COPY quotes-analytics.csproj .

# Restore dependencies
RUN dotnet restore quotes-analytics.sln

# Copy source code (excluding tests via .dockerignore)
COPY . .

# Build and publish
RUN dotnet publish quotes-analytics.csproj -c Release -o /app/publish --no-restore

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app

# Copy published app
COPY --from=build /app/publish .

# Expose port
EXPOSE 8081

# Set environment variables for OpenTelemetry
ENV ASPNETCORE_URLS=http://+:8081
ENV DOTNET_SYSTEM_NET_HTTP_SOCKETSHTTPHANDLER_HTTP2UNENCRYPTEDSUPPORT=true

# Run the application
ENTRYPOINT ["dotnet", "quotes-analytics.dll"]
