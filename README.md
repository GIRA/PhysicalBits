Uzi
===

UziParser is built using PetitParser (http://scg.unibe.ch/research/helvetia/petitparser). To load it into your image, evaluate:
```smalltalk
(Installer lukas project: 'petit')
  install: 'PetitParser';
  install: 'PetitTests'.
```

UziServer uses the REST package (https://github.com/RichoM/REST), which in turn uses WebClient (http://www.squeaksource.com/WebClient/). To load WebClient into your image, evaluate:
```smalltalk
(Installer ss project: 'WebClient')
  install: 'WebClient-Core';
  install: 'WebClient-Tests'.
```
