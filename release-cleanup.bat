@echo off
setlocal
:PROMPT
SET /P AREYOUSURE=Are you sure (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

@echo on
rmdir .git /s /q
rmdir c++ /s /q
rmdir docs /s /q
rmdir st /s /q
rm .gitmodules
rm LICENSE
rm README.md
rm %0 

:END
endlocal


