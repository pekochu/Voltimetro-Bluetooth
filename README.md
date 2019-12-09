# Voltimetro-Bluetooth
Proyecto donde se incluye el código de la aplicación Android configurada para conectar con dispositivos Bluetooth por comunicación serial. 

## ¿Cómo funciona?
La aplicación Bluetooth consta de un Gauge como indicador de voltaje que está midiendo el divisor de voltaje. La señal de entrada es una combinación de números binarios que se convierten a decimal y se usa este número dentro de una ecuación matemática para obtener el voltaje real que se está midiendo de una fuente de corriente directa (DC). 
Adjunto en este repositorio esta el código para Arduino necesario para enviar la combinación binaria por Bluetooth. Se usó un módulo HC-05 para la comunicación serial inalámbrica entre el dispositivo Android y el Arduino. 


## El Arduino
El Arduino recibe 8 señales de un circuito integrado ADC0804 que convierte voltaje de corriente directa en una combinación binaria. Esta combinación se envía al Arduino el cual envía la señal al teléfono Android.

## El dispositivo Android
La aplicación que puedes compilar tu mismo con Android Studio transforma la combinación binaria recibida en un valor que es fácil de leer.

## El Divisor de Voltaje
Se utilizaron dos resistencias (una de 510 y otra de 8.2k Ohm) para que atenue un voltaje de entre 0 - 60v a 0 - 5v. Esto nos permite que el ADC lea el valor para ser convertido.

## Proyecto para la materia de Instrumentación en la ESCOM - IPN

