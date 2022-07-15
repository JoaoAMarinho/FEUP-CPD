cd src || exit 1
C:\\Users\\joaoa\\.jdks\\openjdk-18.0.1.1\\bin\\javac.exe \
  -d ../out \
  -cp ../lib/json-20220320.jar \
  store/*.java \
  communication/*.java \
  communication/election/*.java \
  communication/message/transmition/*.java \
  communication/message/parsing/*.java \
  communication/unicast/*.java \
  communication/message/*.java \
  utils/*.java \
  interfaces/*.java \
  client/*.java
