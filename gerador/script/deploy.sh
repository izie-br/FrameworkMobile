#!/bin/sh

if [ $# -lt 1 ]; then
  echo "Uso: $0 <pasta_base_projeto>"
  exit
fi

GERADOR_SCRIPT_DIR="$( cd -P "$( dirname "$0" )" && pwd )"

SCRIPT_DIR="$1/script"

if [ ! -d "$SCRIPT_DIR" ]; then
    mkdir $SCRIPT_DIR
fi

echo '#!/bin/sh' > $SCRIPT_DIR/gerar.sh
echo "$GERADOR_SCRIPT_DIR/gerar_classes.sh "'"$( cd -P "$( dirname "$0" )" && pwd )/.."' >> $SCRIPT_DIR/gerar.sh
chmod +x $SCRIPT_DIR/gerar.sh

cp -r $GERADOR_SCRIPT_DIR/../customSrc $1

