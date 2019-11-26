@ECHO OFF
REM Location of GTA5.exe
set location="E:\Program Files (x86)\Steam\steamapps\common\Grand Theft Auto V\GTA5.exe"

REM Delete default GTA V rules
netsh advfirewall firewall delete rule name="Grand Theft Auto V"

REM Delete previously created GTA V rules
netsh advfirewall firewall delete rule name="GTA V Block"
netsh advfirewall firewall delete rule name="GTA V Open"

REM Create inbound rules
netsh advfirewall firewall add rule name="GTA V Block" protocol=UDP dir=in action=block enable=no localport=6672,61455,61456,61457,61458 remoteip=0.0.0.0 profile=any program=%location%
netsh advfirewall firewall add rule name="GTA V Block" protocol=TCP dir=in action=block enable=no localport=any remoteip=0.0.0.0 profile=any program=%location%
netsh advfirewall firewall add rule name="GTA V Open" protocol=UDP dir=in action=allow localport=any profile=any program=%location%
netsh advfirewall firewall add rule name="GTA V Open" protocol=TCP dir=in action=allow localport=any profile=any program=%location%

REM Create outbound rules
netsh advfirewall firewall add rule name="GTA V Block" protocol=UDP dir=out action=block enable=no localport=6672,61455,61456,61457,61458 remoteip=0.0.0.0 profile=any program=%location%
PAUSE