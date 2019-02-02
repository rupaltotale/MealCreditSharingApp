#include <Adafruit_BLE.h>
#include <Adafruit_BluefruitLE_SPI.h>
#include <Adafruit_BluefruitLE_UART.h>
#define BUFSIZE                        128   // Size of the read buffer for incoming data
#define VERBOSE_MODE                   true  // If set to 'true' enables debug output


// SOFTWARE UART SETTINGS
// ----------------------------------------------------------------------------------------------
// The following macros declare the pins that will be used for 'SW' serial.
// You should use this option if you are connecting the UART Friend to an UNO
// ----------------------------------------------------------------------------------------------
#define BLUEFRUIT_SWUART_RXD_PIN       -1    // Required for software serial!
#define BLUEFRUIT_SWUART_TXD_PIN       -1   // Required for software serial!
#define BLUEFRUIT_UART_CTS_PIN         -1   // Required for software serial!
#define BLUEFRUIT_UART_RTS_PIN         -1   // Optional, set to -1 if unused


// HARDWARE UART SETTINGS
// ----------------------------------------------------------------------------------------------
// The following macros declare the HW serial port you are using. Uncomment
// this line if you are connecting the BLE to Leonardo/Micro or Flora
// ----------------------------------------------------------------------------------------------
#ifdef Serial1    // this makes it not complain on compilation if there's no Serial1
  #define BLUEFRUIT_HWSERIAL_NAME      Serial1
#endif


// SHARED UART SETTINGS
// ----------------------------------------------------------------------------------------------
// The following sets the optional Mode pin, its recommended but not required
// ----------------------------------------------------------------------------------------------
#define BLUEFRUIT_UART_MODE_PIN        -1    // Set to -1 if unused

// Simple strand test for Adafruit Dot Star RGB LED strip.
// This is a basic diagnostic tool, NOT a graphics demo...helps confirm
// correct wiring and tests each pixel's ability to display red, green
// and blue and to forward data down the line.  By limiting the number
// and color of LEDs, it's reasonably safe to power a couple meters off
// the Arduino's 5V pin.  DON'T try that with other code!

#include <Adafruit_DotStar.h>

#include <SPI.h>         // COMMENT OUT THIS LINE FOR GEMMA OR TRINKET

#define BLUEFRUIT_SPI_CS               -1
#define BLUEFRUIT_SPI_IRQ              -1
#define BLUEFRUIT_SPI_RST              -1

//Adafruit_BluefruitLE_SPI BT(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

SoftwareSerial BT(10, 0); //TX , RX

#define NUMPIXELS 144 // Number of LEDs in strip

#define DATAPIN    9
#define CLOCKPIN   3
Adafruit_DotStar strip = Adafruit_DotStar(
  NUMPIXELS, DATAPIN, CLOCKPIN, DOTSTAR_BGR);

//Adafruit_DotStar strip = Adafruit_DotStar(NUMPIXELS, DOTSTAR_BRG);
int generalDelay = 20, lightIncrementer = 0, lightindexxRain = 0;
int numOfLedsRain = 10;
boolean ldr = false;
boolean rainOn = false, smoothRainOn = false;
boolean lightsOff = true;
boolean stopLights = false;
boolean centerStrip = false;
boolean flash = false;
boolean allUpdate = false;
boolean cyclingColors = false;
boolean certainRainOn = false;
boolean cycleFast = false;
boolean randomColors = false;
boolean noTail = false;
boolean convergingCenter = false;
int tailSaver = -142;
int      head  = 0, tail = -142, brightness = 255;
int head2 = 71, tail2 = -71;
uint32_t black = strip.Color(0,0,0);
uint32_t color = strip.Color(0, 0, 0);  // 'On' color (starts red)
uint32_t red = strip.Color(255,0,0), blue = strip.Color(0,0,255);
uint32_t green = strip.Color(0,128,0), magPurp = strip.Color(160,0,160);
uint32_t yellow = strip.Color(255,255,0), orange = strip.Color(255,69,0);
uint32_t indigo = strip.Color(20,0,180);
const int buttonPin =  2;
boolean buttonOn = false;
uint32_t colorArr[] = {red, orange, yellow, green, blue, magPurp};
uint32_t specialColorArr[9]; int numOfColors = 0;
boolean specialRainOn = false; int counter = 0; int amntOfColors = 0; int holder = 0;
int updateNum = 0; boolean updateCertainNum = false; int counterForUp = 0;

void setup() {
Serial.begin(9600);
BT.begin(9600);
Serial.println("We are gucci");

#if defined(__AVR_ATtiny85__) && (F_CPU == 16000000L)
  clock_prescale_set(clock_div_1); // Enable 16 MHz on Trinket
#endif

delay(40);
  strip.begin(); // Initialize pins for output
  //for(int a = 0; a < NUMPIXELS; a++) { turnOff(); }
  strip.show();  // Turn all LEDs off ASAP
}

// Runs 10 LEDs at a time along strip, cycling through red, green and blue.
// This requires about 200 mA for all the 'on' pixels + 1 mA per 'off' pixel.
void turnOff()
{
  for(int i = 0; i < NUMPIXELS; i++)
  {
    strip.setPixelColor(i, 0);
  }
  strip.show();
}

void turnColor(uint32_t tempCol)
{
  for(int i = 0; i < NUMPIXELS; i++)
  {
    strip.setPixelColor(i, tempCol);
  }
}

void changeBrightness(String brightVar)
{
  brightness = brightVar.toInt();
  strip.setBrightness(brightness);
  Serial.print("Brightness set to: ");
  Serial.println(brightness);
}

void colorMaker(String str)
{
  if(str.equals("yellow")) { color = yellow; return;}
      if(str.equals("red")){ color = red; return;}
      if(str.equals("green")){ color = green; return;}
      if(str.equals("blue")) {color = blue; return;}
      if(str.equals("mp")){ color = magPurp; return;}
      if(str.equals("orange")){color=orange; return;}
     if(str.equals("indigo")){color=indigo;}
}

void stripGo()
{
  strip.setPixelColor(head, color); // 'On' pixel at head
  strip.setPixelColor(tail, 0);     // 'Off' pixel at tail
  //strip.show(); // Refreshes the strip
  //delay(20); // Pause 20 milliseconds (~50 FPS)
  if(++head >= NUMPIXELS) {         // Increment head index.  Off end of strip?
    head = 0;                       //  Yes, reset head index to start
    /*if((color >>= 8) == 0)          //  Net color (R->G->B) ... past blue now?
      color = 0FF0000;*/            //   Yes, reset to red
  }
  if(++tail >= NUMPIXELS) tail = 0; // Increment, reset tail index
}

/*void updateCertainLEDs(int leds)
{
  
}*/

void stripGoNoTail()
{
  strip.setPixelColor(head, color); // 'On' pixel at head
  //strip.show(); // Refreshes the strip
  //delay(20); // Pause 20 milliseconds (~50 FPS)
  if(++head >= NUMPIXELS) {         // Increment head index.  Off end of strip?
    head = 0;                       //  Yes, reset head index to start
    /*if((color >>= 8) == 0)          //  Net color (R->G->B) ... past blue now?
      color = 0FF0000;*/            //   Yes, reset to red
  }
}

void centerStripGo()
{
  if(convergingCenter) { convergeToCenter(); return; }
  strip.setPixelColor(head, color);
  strip.setPixelColor(head2, color);// 'On' pixel at head
  strip.setPixelColor(tail, 0); // 'Off' pixel at tail
  strip.setPixelColor(tail2, 0);
  head2++;
  tail2++;
  //strip.show(); // Refreshes the strip
  //delay(20); // Pause 20 milliseconds (~50 FPS)
  if(--head < 0) {         // Increment head inde.  Off end of strip?
    head = 71; head2 = 72;                       //  Yes, reset head inde to start
  }
  if(tail < 0) {tail++; return;}
  if(--tail < 0) { tail = 71; tail2 = 72; }
}

void convergeToCenter() {
  strip.setPixelColor(head, color);
  strip.setPixelColor(head2, color);// 'On' pixel at head
  strip.setPixelColor(tail, 0); // 'Off' pixel at tail
  strip.setPixelColor(tail2, 0);
  head2--;
  tail2--;
  //strip.show(); // Refreshes the strip
  //delay(20); // Pause 20 milliseconds (~50 FPS)
  if(++head > 71) {         // Increment head inde.  Off end of strip?
    head = 0; head2 = 143;                       //  Yes, reset head inde to start
  }
  if(tail < 0) {tail++; return;}
  if(++tail > 71) { tail = 0; tail2 = 143; }
}

void convergeToCenterNoTail() {
  strip.setPixelColor(head, color);
  strip.setPixelColor(head2, color);// 'On' pixel at head
  head2--;
  if(++head > 71) {         // Increment head inde.  Off end of strip?
    head = 0; head2 = 143;                       //  Yes, reset head inde to start
  }
}

void centerStripGoNoTail()
{
  if(convergingCenter) { convergeToCenterNoTail(); return; }
  strip.setPixelColor(head, color);
  strip.setPixelColor(head2, color);// 'On' pixel at head
  head2++;
  if(--head < 0) {         // Increment head inde.  Off end of strip?
    head = 71; head2 = 72;                       //  Yes, reset head inde to start
  }
}

void clearColors()
{
  if(centerStrip && !convergingCenter) { turnOff(); head = 71; tail = tailSaver; head2 = 72; tail2 = tailSaver; Serial.println("Cleared Strip"); return;}
  else if(centerStrip && convergingCenter) {turnOff(); head = 0; tail = tailSaver; head2 = 143; tail2 = 213 - (72 + tailSaver); Serial.println("Cleared Strip"); return; }
  turnOff();
  head = 0; tail = tailSaver;
  Serial.println("Cleared Strip");
}

void turnOffBooleanRains()
{
  rainOn = false;
  smoothRainOn = false;
  specialRainOn = false;
  lightIncrementer = 0;
      lightindexxRain  = 0;
      counter = 0;
}

void doSpecialRainbow(int numOfCols, String str)
{
  numOfColors = numOfCols;
  for(int i = 0; i < numOfCols; i++)
  {
    int red = str.substring(0, 3).toInt();
    int green = str.substring(4, 7).toInt();
    int blue = str.substring(8, 11).toInt();
    specialColorArr[i] = strip.Color(red, green, blue);
    str = str.substring(12);
  }
}

String createSpecialRainString(int numHolder)
{
  String str = "";
  String saver = "";
  int countArr = 0;
  while(countArr < numHolder)
  {
    while(BT.available() && BT.read() != 0 && Serial.read() == -1)
    {
     delay(2);
    char incomingChar = BT.read();
    saver += incomingChar;
    }
    if(saver.length() > 8) {
    countArr++;
    str += saver; saver = "";}
  }
  return str;
}

String serialOpen()
{
  if(Serial.available() > 0 && (!Serial.read() != -1))
  {
    Serial.println(Serial.readStringUntil('x'));
    return Serial.readStringUntil('x');
  }
  return "";
}

String str = "";
String str1 = "";
void stringTakeDo()
{
  if((!Serial.available()) && (!Serial.read() != -1))
  {
    while(BT.available() && BT.read() != 0 && Serial.read() == -1)
    {
      delay(2);
    char incomingChar = BT.read();
    str += incomingChar;
    }
  }
  else if(Serial.available() && Serial.read() > -1)
  {
    str = Serial.readString();
  }
  if(str.charAt(0) == ' ') { return; }
  char c = str.charAt(0); if(isDigit(c)) { str = ""; return; }
  if(str.length() > 0)
  {
    Serial.println(str);
    if(str.equals("begin")) { strip.begin(); str = "";return; }
    if(str.equals("clear")) { strip.clear(); str = "";return; }
    if(str.equals("where?")) { Serial.println(head); Serial.println(tail); Serial.println(head2); Serial.println(tail2); str = ""; return; }
    if(str.equals("color?")) { Serial.println(strip.getPixelColor(head - 1)); str = "";return; }
    if(str.equals("rainbow")) {turnOffBooleanRains(); rainOn = true; lightsOff = false; str = "";return; }
    if(str.equals("comp random")) { doCompleteRandomGen(); Serial.println(generalDelay); Serial.println(brightness); Serial.println(tail); str = ""; return; }
    if(str.equals("q"))
    {
      turnOff();
      Serial.println("Quitting");
      head = 0; tail = -142;
      delay(100);
      exit(0);
    }
    else if(str.equals("every LED")) { counterForUp = 0; updateCertainNum = false; allUpdate = true; str = "";return; }
    else if(str.equals("single LED")) { counterForUp = 0; updateCertainNum = false; allUpdate = false; str = "";return; }
    else if(str.equals("cycle colors")) { cyclingColors = true; str = "";return; }
    else if(str.substring(0, 6).equals("update")) { counterForUp = 0; allUpdate = false; updateCertainNum = true; updateNum = str.substring(7).toInt(); Serial.print("Updating "); Serial.print(updateNum); Serial.print(" LEDs"); str = ""; return; }
    if(str.substring(0, 7).equals("special"))
    {
      turnOffBooleanRains();
      specialRainOn = true;
      str += createSpecialRainString(str.substring(8, 9).toInt());
      Serial.println(str.substring(10));
      doSpecialRainbow(str.substring(8, 9).toInt(), str.substring(10));
      lightsOff = false;
      str = "";
      return;
    }
    if(str.equals("smooth rain"))
    {
      turnOffBooleanRains();
      smoothRainOn = true;
      lightsOff = false;
      str = "";
      return;
    }
    if(str.equals("stop moving"))
    {
      stopLights = true;
      str = "";
      return;
    }
    if(str.equals("go"))
    {
      stopLights = false;
      str = "";
      return;
    }
    if(str.equals("ldr on"))
    {
      ldr = true;
      str = "";
      return;
    }
    if(str.equals("ldr off"))
    {
      ldr = false;
      str = "";
      return;
    }
    if(str.equals("full color stop"))
    {
      turnColor(color);
      strip.show();
      stopLights = true;
      str = "";
      return;
    }
    if(str.equals("cycle fast")) { cycleFast = true; str = ""; return;}
    if(str.equals("how bright") || str.equals("how fast"))
    {
      if(str.equals("how bright"))
      {
        Serial.println(strip.getBrightness());
        str = "";
        return; 
      }
      else
      {
        Serial.println(generalDelay);
        str = "";
        return;
      }
    }
    if(str.substring(0, 9).equals("randomize")) {
      turnOffBooleanRains(); 
      amntOfColors = str.substring(10).toInt(); 
      holder = 144 / amntOfColors;
      randomColors = true;
      lightsOff = false;
      color = randomColoring();
      str = "";
      return; 
    }
    if(str.substring(0, 11).equals("change tail") && !buttonOn)
    {
      noTail = false;
      tail = str.substring(11).toInt() * -1;
      tailSaver = tail;
      clearColors();
      Serial.print("Tail is: ");
      Serial.println(tail);
      str = "";
      return;
    }
    if(str.equals("stop"))
    {
      clearColors();
      lightsOff = true;
      rainOn = false;
      buttonOn = false;
      specialRainOn = false;
      strip.setBrightness(brightness);
      flash = false;
      randomColors = false;
      stopLights = false;
      smoothRainOn = false;
      cyclingColors = false;
      cycleFast = false;
      lightIncrementer = 0;
      lightindexxRain  = 0;
      counter = 0;
      counterForUp = 0;
      str = "";
      return;
    }
    if(str.equals("center light")) {
      clearColors();
      centerStrip = true;
      head = 71; head2 = 72;
      tail = -70; tail2 = -70;
      str = "";
      return;
    }
    else if(str.equals("converge")) { convergingCenter = true; clearColors(); head = 0; head2 = 143; tail = tailSaver; tail2 = 213 - (72 + tailSaver); str = ""; return; }
    else if(str.equals("diverge")) { convergingCenter = false; clearColors(); head = 71; head2 = 72; tail = tailSaver; tail2 = tailSaver; str = ""; return; }
    else if(str.equals("no center")) { clearColors(); centerStrip = false; head = 0; tail = tailSaver; str = ""; return; }
    if(str.substring(0, 10).equals("brightness"))
    {
      changeBrightness(str.substring(10, str.length()));
      str = "";
      return;
    }
    if(str.substring(0, 5).equals("color")) { 
      int col1 = str.substring(6, 9).toInt(); 
      int col2 = str.substring(10, 13).toInt(); 
      int col3 = str.substring(14, 17).toInt();
      color = strip.Color(col1, col2, col3);
      lightsOff = false;
      str = "";
      return;
    }
    if(str.equals("no tail")) { noTail = true; str = ""; return;}
    else if(str.equals("tail")) { noTail = false; str = ""; return; }
    if(str.equals("flash") && !buttonOn) { flash = true;   str = ""; return;}
    if(str.substring(0, 5).equals("delay")) { 
      generalDelay = str.substring(5).toInt(); 
      Serial.print("Delay is: "); 
      Serial.println(generalDelay);
      str = ""; 
      return;
}
    if(!(str.equals("q") || str.equals("stop")))
    {
      lightsOff = false;
      turnOffBooleanRains();
      colorMaker(str);
    }
  }
  str = "";
}

void doRainbow(int leds)
{
  color = colorArr[lightindexxRain];
  if(lightIncrementer == leds)
  {
    lightIncrementer = 0;
    lightindexxRain++;
    if(lightindexxRain == 6)
    {
      lightindexxRain = 0;
    }
  }
}

void doFullReset() {
  clearColors();
      lightsOff = true;
      rainOn = false;
      buttonOn = false;
      specialRainOn = false;
      strip.setBrightness(brightness);
      flash = false;
      stopLights = false;
      smoothRainOn = false;
      cyclingColors = false;
      cycleFast = false;
      lightIncrementer = 0;
      lightindexxRain  = 0;
      counterForUp = 0;
      counter = 0;
      allUpdate = false;
      centerStrip = false;
      noTail = false;
      randomSeed(analogRead(A5));
}

void doCompleteRandomGen() {
  doFullReset();
  long holder;
  long randomNum = random(0, 100);
  holder = randomNum;
  if(randomNum >= 60) { //1 color but random
    color = randomColoring();
  }
  else if(randomNum < 60 && randomNum >= 25) { //rain
    randomSeed(analogRead(A5));
     randomNum = random(0, 2);
     if(randomNum == 0) { smoothRainOn = true; }
     else if(randomNum == 1) { rainOn = true; }
  }
  else if(randomNum < 25 && randomNum >= 5) { //full color stop
    color = randomColoring();
    randomSeed(analogRead(A5));
    strip.setBrightness((int)random(20, 255));
    turnColor(color);
    strip.show();
    stopLights = true;
    return;
  }
  else //cycle colors
  {
    randomSeed(analogRead(A5));
    randomNum = random(0, 2);
    if(randomNum == 0) { cyclingColors = true; return; }
    else if(randomNum == 1) {cycleFast = true; return; } 
  }
randomSeed(analogRead(A5));
  randomNum = random(0, 2);
  if(randomNum == 1 && holder < 60) { allUpdate = true; } //Every LED or Single LED
  randomSeed(analogRead(A5));
  randomNum = random(0, 2); // Below is for centerStrip then converging vs diverging
  if(randomNum == 1) { centerStrip = true; head = 71; head2 = 72;
      tail = -70; tail2 = -70; randomSeed(analogRead(A5)); randomNum = random(0, 2); if(randomNum == 1) { randomSeed(analogRead(A5)); tailSaver = (int)random(1, 71) * -1; convergingCenter = true; clearColors(); head = 0; head2 = 143; tail = tailSaver; tail2 = 213 - (72 + tailSaver);}
  else if(randomNum == 0) { randomSeed(analogRead(A5)); tailSaver = (int)random(1, 71) * -1; convergingCenter = false; head = 71; head2 = 72; tail = tailSaver; tail2 = tailSaver;} }
  else if(randomNum == 0) { randomSeed(analogRead(A5)); tail = (int)random(1, 143) * -1; tailSaver = tail; clearColors();}
  randomSeed(analogRead(A5));
  randomNum = random(0, 100);
  if(randomNum >= 70 && holder < 60) {
    noTail = true;
  }
  if(allUpdate) {
    randomSeed(analogRead(A5));
    randomNum = random(50, 200);
    generalDelay = (int)randomNum;
    brightness = (int)(random(20, 255));
    strip.setBrightness(brightness);
    lightsOff = false;
    return;
  }
  else {
    randomSeed(analogRead(A5));
    randomNum = random(5, 25);
    generalDelay = (int)randomNum;
    brightness = (int)(random(20, 255));
    strip.setBrightness(brightness);
  }

  lightsOff = false;
}

void doSpecialRain()
{
  color = specialColorArr[lightindexxRain];
  if(lightIncrementer == (144 / numOfColors))
  {
    lightIncrementer = 0;
    lightindexxRain++;
    if(lightindexxRain == numOfColors)
    {
      lightindexxRain = 0;
    }
  }
}

void showDelay(int delayAmnt)
{
  strip.show();
  delay(delayAmnt);
}

int tempBright;
void flashOnOff()
{
  tempBright = brightness;
    strip.setBrightness(0);
    strip.show();
    delay(generalDelay);
    strip.setBrightness(tempBright);
    //strip.show();
    //delay(generalDelay);
    tempBright = 0;
}

void smoothRain()
{
  int col1 = 0, col2 = 0, col3 = 0;
  if(lightIncrementer <= 255) {
    col1 = 255, col2 = 0, col3 = 0;
    color = strip.Color(col1 - lightIncrementer, col2 + lightIncrementer, col3);
    return;
  }
  else if(lightIncrementer >= 255 && lightIncrementer <= 510) {
      col1 = 0; col2 = 255; col3 = 0;
      color = strip.Color(col1, col2 - lightIncrementer - 255, col3 + lightIncrementer - 255);
      return;
  }
  else if(lightIncrementer >= 510 && lightIncrementer < 765){
    col1 = 0; col2 = 0; col3 = 255;
      color = strip.Color(col1 + lightIncrementer - 510, col2, col3 - lightIncrementer - 510);
      return;
  }
  else { lightIncrementer = 0; return; }
  
}
void reactive(int delayTime)
{
  turnColor(color);
  strip.setBrightness(0);
  for(int i = 0; i < 255; i++)
  {
    strip.setBrightness(i);
    strip.show();
    delay(delayTime);
    //Serial.println(strip.getBrightness());
  }
  strip.setBrightness(255);
  strip.show();
  //Serial.print("-Max bright");
  delay(10);
  for(int i = 254; i > -1; i--)
  {
    strip.setBrightness(i);
    strip.show();
    delay(delayTime);
  }
}

void cycleColors()
{
  reactive(1);
  color = colorArr[lightindexxRain];
  lightindexxRain++;
  if(lightindexxRain == 6)
  {
    lightindexxRain = 0;
  }
  return;
}

void cyclingFast()
{
  turnColor(color);
  color = colorArr[lightindexxRain];
  lightindexxRain++;
  if(lightindexxRain == 6)
  {
    lightindexxRain = 0;
  }
  strip.show();
  return;
}

uint32_t randomColoring() {

    long randomZero = random(0, 3);
    if(randomZero == 0) {
      return strip.Color(0, random(0, 256), random(0, 256));
    }
    else if(randomZero == 1) {
      return strip.Color(random(0, 256), 0, random(0, 256));
    }
    else {
      return strip.Color(random(0, 256), random(0, 256), 0);
    }
}

void loop() {
  stringTakeDo();
  if((!ldr) || ((analogRead(A0) < 80) && ldr))
  {
  if(flash)
  {
    flashOnOff();
  }
  if(cyclingColors || cycleFast)
  {
    if(cyclingColors) { cycleColors(); }
    else if(cycleFast) { cyclingFast(); }
    delay(generalDelay);
    return;
  }
  if(lightsOff || buttonOn || stopLights)
  {
    return;
  }
  if(rainOn)
  {
    doRainbow(numOfLedsRain);
    lightIncrementer++;
  }
  if(smoothRainOn)
  {
    smoothRain();
    lightIncrementer += 5;
  }
  else if(specialRainOn)
  {
    doSpecialRain();
    lightIncrementer++;
  }
  if(randomColors) { counter++; }
  if(counter == holder && randomColors) { color = randomColoring(); counter = 0;}
  if((allUpdate || updateCertainNum) && (!centerStrip))
  {
    if(noTail) 
    { 
      stripGoNoTail(); 
    }
    else { stripGo(); }
    if(allUpdate) {
       if(head == NUMPIXELS - 1)
      {
        strip.show();
        delay(generalDelay);
      }
    }
    else { 
      if(counterForUp == updateNum) 
        { strip.show(); delay(generalDelay); counterForUp = 0;}
        else {
          counterForUp++;
        }
    }
    return;
  }
  else if((allUpdate || updateCertainNum) && centerStrip) { centerStripGo(); 
  if(allUpdate) {
       if(head == NUMPIXELS - 1)
      {
        strip.show();
        delay(generalDelay);
      }
    }
    else { 
      if(counterForUp == updateNum) 
        { strip.show(); delay(generalDelay); counterForUp = 0;}
        else {
          counterForUp++;
        }
    } return; }
  if(centerStrip)
  {
    if(!noTail) { centerStripGo(); } else {centerStripGoNoTail(); }
    strip.show();
    delay(generalDelay);
    return;
  }
  if(noTail) 
    { 
      stripGoNoTail();
    }
    else { stripGo(); }
  strip.show();
  delay(generalDelay);
  }
  else if(ldr && analogRead(A0 > 80))
  {
    turnOff();
  }
}

