# Multi-stage Dockerfile para .NET 8 ASP.NET Core Web API + SignalR

# 1. Etapa de compilación (Build)
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copiar archivo de proyecto y restaurar dependencias
COPY ["CommunityRideBackend.csproj", "./"]
RUN dotnet restore "CommunityRideBackend.csproj"

# Copiar el resto del código y publicar
COPY . .
RUN dotnet publish "CommunityRideBackend.csproj" -c Release -o /app/publish /p:UseAppHost=false

# 2. Etapa de ejecución (Runtime)
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app

# Puerto por defecto (se adapta dinámicamente si la plataforma pasa la variable $PORT)
ENV PORT=8080
EXPOSE 8080
EXPOSE 5000

# Copiar binarios compilados
COPY --from=build /app/publish .

# Comando de inicio
ENTRYPOINT ["dotnet", "CommunityRideBackend.dll"]

