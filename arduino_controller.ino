#include <SoftwareSerial.h>

SoftwareSerial SerialBluetooth(2, 3); // RX | TX
int i = 0;
String binOutput = "";

void setup() {
  Serial.begin(9600);
  SerialBluetooth.begin(9600);

  pinMode(22, INPUT);
  pinMode(24, INPUT);
  pinMode(26, INPUT);
  pinMode(28, INPUT);
  pinMode(30, INPUT);
  pinMode(32, INPUT);
  pinMode(34, INPUT);
  pinMode(36, INPUT);
}

void loop() {  
  
  // Leer el estado de los pines
  for(i = 22; i < 37; i += 2){
    binOutput.concat(digitalRead(i) == HIGH ? "1" : "0");
  }
  // Agregar fin de linea para transmisión Bluetooth
  binOutput.concat("$");

  Serial.write("Bin output obtenido: ");
  SerialBluetooth.print(binOutput);
  Serial.println(binOutput);
  binOutput = "";
  delay(500);  
}
