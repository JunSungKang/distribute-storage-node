# Storage Node

## Preference setting
- SetJDK : JDK17 - aws production <br/>
  https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html
- Reed-Solomon Jar Download (Fork Link) <br/>
  https://github.com/JunSungKang/JavaReedSolomon/releases/tag/Release-v.1.0

## Build
1. Reed-Solomon jar move <br/>
   `${project.basedir}/lib/JavaReedSolomon.jar`
2. mvn package <br/>
   ※ Skip the mvn test if you can't do the ethereum testnet.