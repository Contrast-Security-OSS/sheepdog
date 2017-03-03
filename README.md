Sheepdog
========

<img src="http://contrast-security-oss.github.io/meow/images/sheepdog-cat.png" alt="Image of SheepDogCat" width="200"/>

Sheepdog is a simple tool to generate normal and attack traffic for OWASP WebGoat. It can be used with security technologies like WAF and RASP in demonstrations and to verify that they are doing a tiny piece of what they are supposed to do. Sheepdog is not intended to be an exhaustive set of security tests. It has some basic SQL injection, XSS, path traversal, and that kind of thing.

Simply start WebGoat with:

> java -jar webgoat-container-7.0.1-war-exec.jar

Then in another window start sheepdog with:

> java -jar sheepdog-1.0.jar

There are several configurable properties that you can use to simulate a variety of crawls/attacks. Note that SheepDog sends an X-Forwarded-For header with random IP address for each attack thread.

> Usage: java -jar sheepdog.jar
   -t threads (default 3)
   -s seconds (default 60)
   -d delay milliseconds between requests (default -1)
   -a attack percentage (default 50)
   -p port for WebGoat (default 8080)



## Sample usage

	$ java -jar target/sheepdog-1.0-SNAPSHOT.jar -t 3 -s 3600 -d 1000 -a 50
        Usage: java -jar sheepdog.jar [-t -s -d -a -p -v]
        Using default value for flag 'p', using 8080
        Starting 3 attack threads, each with:
          3600 seconds
          1000ms delay between requests
          50% attack parameters
          target: http://localhost:8080/WebGoat/

          Starting AttackThread (110.104.52.59) 
	  Starting AttackThread (93.65.24.224)
	  Starting AttackThread (161.144.64.146)
	
	POST from 238.20.254.102 to http://localhost:8080/WebGoat/j_spring_security_check
	   [username=guest, password=guest]
	   HTTP/1.1 302 Found
	
	POST from 93.65.24.224 to http://localhost:8080/WebGoat/j_spring_security_check
	   [username=guest, password=guest]
	   HTTP/1.1 302 Found
	
	POST from 161.144.64.146 to http://localhost:8080/WebGoat/j_spring_security_check
	   [username=guest, password=guest]
	   HTTP/1.1 302 Found
	
	POST from 161.144.64.146 to http://localhost:8080/WebGoat/attack?Screen=733&menu=1200
	   [SUBMIT=zoees822]
	   HTTP/1.1 200 OK
	
	POST from 93.65.24.224 to http://localhost:8080/WebGoat/attack?Screen=534&menu=1900
	   [id=sztol903, SUBMIT=rjeee272]
	   HTTP/1.1 200 OK
	
	POST from 161.144.64.146 to http://localhost:8080/WebGoat/attack?Screen=726&menu=200&stage=1
	   [action=' or 112=112--]
	   HTTP/1.1 200 OK
	
	POST from 161.144.64.146 to http://localhost:8080/WebGoat/attack?Screen=737&menu=1100&stage=3
	   [action=' or 1+2=3 --]
	   HTTP/1.1 200 OK
	
	POST from 93.65.24.224 to http://localhost:8080/WebGoat/attack?Screen=498&menu=1300
	   [clear_user=><script>alert(1)</script>, clear_pass=><script>alert(1)</script>, Submit=ctyna446]
	   HTTP/1.1 200 OK



## Who made this?
This project is sponsored by [Contrast Security](http://www.contrastsecurity.com/) and released under the MIT license.

![Contrast Security Logo](https://www.contrastsecurity.com/hs-fs/hubfs/Contrast_Security-2016/Image/LOGOContrastSec.SVG.svg?t=1488557782361&width=199&name=LOGOContrastSec.SVG.svg "Contrast Logo")
