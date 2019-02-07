//config 
//pin to look for change that marks a tick of UZI's VM
const int triggerPin=53;
//array containing the pins to monitor.
const byte pinMap[]{43,45,47,49,51};
const byte pinCount=sizeof(pinMap)/sizeof(*pinMap);

struct spec{
    long ms;
    byte pins=0;
    spec* next=0;
  };

void setup() {
  Serial.begin(57600);
  for(int i=0;i<=pinCount;i++){
    pinMode(pinMap[i],INPUT);
  }
  pinMode(triggerPin,INPUT);
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

  while(targetTime==0){
    delay(100);
    Serial.println("Ready. Waiting for target time");
    targetTime=readLong();
    }
  Serial.print("Capturing ");
  Serial.print(targetTime);
  Serial.println(" ms");
  int currentTime=0;
  long startms=0;
  bool lastState=digitalRead(triggerPin);
  while(currentTime<targetTime){
    
     currentTime=millis();
     if(startms==0){startms=currentTime;}
     currentTime-=startms;
     if(currentTime>targetTime){
      break;
      }
     bool state=digitalRead(triggerPin);
     if(state==lastState){continue;}
     lastState=state;
     byte pins=0;
     for(int i=0;i<=pinCount;i++){
       pins|= (digitalRead(pinMap[i]))<<(8-i);
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
