FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY quotes-analytics.csproj .
RUN dotnet restore quotes-analytics.csproj
COPY . .
RUN dotnet publish quotes-analytics.csproj -c Release -o /app/publish --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8081
ENV ASPNETCORE_URLS=http://+:8081
ENV DOTNET_SYSTEM_NET_HTTP_SOCKETSHTTPHANDLER_HTTP2UNENCRYPTEDSUPPORT=true
ENTRYPOINT ["dotnet", "quotes-analytics.dll"]
