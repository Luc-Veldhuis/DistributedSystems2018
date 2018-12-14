import sys

content = ""
with open("../HeadNode/build/resources/main/application.conf", "r+") as application:
    content = application.read()
content = content.replace("0.0.0.0", "node"+sys.argv[1])
print content
with open("../HeadNode/build/resources/main/application.conf", "w+") as application:
    application.write(content)
