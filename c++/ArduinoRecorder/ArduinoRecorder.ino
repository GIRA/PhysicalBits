//config 
//pin to look for change that marks a tick of UZI's VM
const int triggerPin=2;
//array containing the pins to monitor.
const byte pinMap[]{22,23,24,25,26,27,28,29,30,31,32,33};
const byte pinCount=sizeof(pinMap)/sizeof(*pinMap);

struct spec{
    long ms;
    byte pins[pinCount];
    int error=0;
  };

void setup() {
  Serial.begin(9600);
  for(int i=0;i<=pinCount;i++){
    pinMode(pinMap[i],INPUT);
  }
  pinMode(triggerPin,INPUT);
}

spec* capturedData=0;

long readLong(){
  long result =Serial.parseInt();
  return result;
  }
void loop() { 
  //read amount 
  int amount=0;

  Serial.println("Ready. Waiting for amount");
  while(amount==0){
    amount=readLong();
    }
  Serial.print("Waiting for ");
  Serial.print(amount);
  Serial.println(" requests");
  capturedData=new spec[amount];
  for(int i=0;i<amount;i++){
    capturedData[i].ms=readLong();
    }
  Serial.println("Finished Reading. Starting Capture");
  int current=0;
  long startms=0;
  bool lastState=digitalRead(triggerPin);
  while(current<amount){
     bool state=digitalRead(triggerPin);
     if(state==lastState){continue;}
     lastState=state;
     unsigned long ms=millis();
     if(startms==0){startms=ms;}
     ms-=startms;
     if(capturedData[current].ms>ms){continue;}
     
     for(int i=0;i<=pinCount;i++){
       capturedData[current].pins[i]=digitalRead(pinMap[i]);
       }
     capturedData[current].error=ms-capturedData[current].ms;
      
     byte* values = &capturedData[current].pins[0];
     
     current++;
     //this loop copies the read values and consumes every spec whose time is already passed
     while(capturedData[current].ms<=ms){
       memcpy(&capturedData[current].pins,values,pinCount);
       capturedData[current].error=ms-capturedData[current].ms;
       current++;
       }
   }
   
   Serial.println("Finished Capture");
   for(int i=0;i<amount;i++){
     Serial.print(capturedData[i].ms);
     Serial.print(",");
     for(int j=0;j<pinCount;j++){
       Serial.print(capturedData[i].pins[j]);
       Serial.print(",");
     }
     Serial.print(capturedData[i].error);
     Serial.println();
   }
   delete capturedData;
   capturedData=0;
}
