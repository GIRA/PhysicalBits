//config 
//pin that enables or disables the connection to the other arduino
const int enablePin=53;
//pin to look for change that marks a tick of UZI's VM
const int triggerPin=51;
//array containing the pins to monitor.
const byte pinMap[]{41,43,45,47,49};
const byte pinCount=sizeof(pinMap)/sizeof(*pinMap);

struct spec{
    long ms;
    byte pins=0;
    spec* next=0;
  };

void setup() {
  Serial.begin(57600);
  for(int i=0;i<=pinCount;i++){
    pinMode(pinMap[i],INPUT_PULLUP);
  }
  pinMode(triggerPin,INPUT_PULLUP);
}

spec* capturedData=0;
spec* lastSpec=0;
long readLong(){
  long result =Serial.parseInt();
  return result;
  }
void loop() { 
  //read amount 
  int targetTime=0;
  byte pinFlags=0;
  while(targetTime==0){
    delay(100);
    Serial.println("Ready. Waiting for target time");
    targetTime=readLong();
    }
  while(pinFlags==0){
    
    Serial.println("Reading pins flags");
    pinFlags=readLong();
    }
  Serial.print("Capturing ");
  Serial.print(targetTime);
  Serial.println(" ms");
  int currentTime=0;
  long startms=0;
  bool lastState=digitalRead(triggerPin);
  while(currentTime<targetTime){
     bool state=digitalRead(triggerPin);
     if(state==lastState){continue;}
     lastState=state;
     currentTime=millis();
     if(startms==0){startms=currentTime;}
     currentTime-=startms;
     if(currentTime>targetTime){
      break;
      }
     
     byte pins=0;
     for(int i=0;i<=pinCount;i++){
      if((pinFlags>>i &1) !=0){
        pins|= (digitalRead(pinMap[i]))<<i;
        }
     }
     if(capturedData==0){
      
      capturedData=new spec(); 
      lastSpec=capturedData;
     }else{
     if(lastSpec->pins==pins){
        continue;
      } 
      spec* newSpec = new spec();
      lastSpec->next=newSpec;
      lastSpec=newSpec;
     }
     
     lastSpec->ms=currentTime;
     lastSpec->pins=pins;
      
   }
   Serial.println("Finished Capture");
   lastSpec=0;
   while(capturedData!=0){
     Serial.print(capturedData->ms);
     Serial.print(","); 
     Serial.print(capturedData->pins);
     Serial.println();
     spec* n = capturedData->next; 
     delete capturedData;
     capturedData=n;
   }  
   capturedData=0;
   
}
