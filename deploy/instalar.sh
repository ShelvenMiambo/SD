#!/usr/bin/env bash
# Instala o Java 17, o Apache Tomcat 10.1 (porta 8081) e o AgroGest num servidor Ubuntu/Debian.
#
# Uso (a partir da raiz do projecto, depois de "./mvnw package"):
#   sudo bash deploy/instalar.sh target/agrogest.war
set -euo pipefail

WAR="${1:-target/agrogest.war}"
TOMCAT_VERSAO="${TOMCAT_VERSAO:-10.1.61}"
PORTA="${PORTA:-8081}"
DIR=/opt/tomcat

if [[ $EUID -ne 0 ]]; then
    echo "Execute com sudo." >&2
    exit 1
fi
if [[ ! -f "$WAR" ]]; then
    echo "WAR não encontrado: $WAR (corra ./mvnw package primeiro)" >&2
    exit 1
fi

echo "==> Java 17"
apt-get update -qq
apt-get install -y -qq openjdk-17-jdk-headless curl

echo "==> Utilizador de sistema 'tomcat'"
id tomcat >/dev/null 2>&1 || useradd -r -m -U -d "$DIR" -s /bin/false tomcat

if [[ ! -x "$DIR/bin/catalina.sh" ]]; then
    echo "==> Apache Tomcat $TOMCAT_VERSAO"
    tmp=$(mktemp -d)
    url="https://archive.apache.org/dist/tomcat/tomcat-10/v$TOMCAT_VERSAO/bin/apache-tomcat-$TOMCAT_VERSAO.tar.gz"
    curl -fsSL "$url" -o "$tmp/tomcat.tar.gz"
    curl -fsSL "$url.sha512" | awk '{print $1"  '"$tmp"'/tomcat.tar.gz"}' | sha512sum -c -
    mkdir -p "$DIR"
    tar -xzf "$tmp/tomcat.tar.gz" -C "$DIR" --strip-components=1
    rm -rf "$tmp" "$DIR"/webapps/{docs,examples}
fi

echo "==> Porta $PORTA"
sed -i -E "s/<Connector port=\"[0-9]+\" protocol=\"HTTP\/1.1\"/<Connector port=\"$PORTA\" protocol=\"HTTP\/1.1\"/" "$DIR/conf/server.xml"

echo "==> Deploy do WAR"
mkdir -p "$DIR/data"
rm -rf "$DIR/webapps/agrogest"
cp "$WAR" "$DIR/webapps/agrogest.war"
chown -R tomcat:tomcat "$DIR"
chmod +x "$DIR"/bin/*.sh

echo "==> Serviço systemd"
cp "$(dirname "$0")/tomcat.service" /etc/systemd/system/tomcat.service
JAVA_DIR=$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")
sed -i "s|^Environment=\"JAVA_HOME=.*\"|Environment=\"JAVA_HOME=$JAVA_DIR\"|" /etc/systemd/system/tomcat.service
systemctl daemon-reload
systemctl enable tomcat
systemctl restart tomcat

echo
echo "Pronto: http://$(hostname -I | awk '{print $1}'):$PORTA/agrogest"
echo "Estado: sudo systemctl status tomcat"
