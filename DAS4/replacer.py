import sys
import re

content = ""
with open("../HeadNode/src/main/resources/application.conf", "r+") as application:
    content = application.read()
print content
pattern = re.compile(r"hostname = \"(.*)\"")
content = pattern.sub('hostname = "'+sys.argv[1]+'"', content)
print content
with open("../HeadNode/src/main/resources/application.conf", "w+") as application:
    application.write(content)
