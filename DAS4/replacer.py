import sys

content = ""
with open("../HeadNode/src/main/resources/application.conf", "r+") as application:
    content = application.read()
print content
content = content.replace("0.0.0.0", sys.argv[1])
print content
with open("../HeadNode/src/main/resources/application.conf", "w+") as application:
    application.write(content)
