import argparse
import os

#setup arguments

parser = argparse.ArgumentParser()
parser.add_argument("file")

args = parser.parse_args()

# Filepath
filename = args.file
filepath = ""
if filename.endswith(".jpg") | filename.endswith(".png"):
	filepath = "/sdcard/DCIM/Camera/" + filename
else:
	filepath = "/sdcard/DCIM/Camera/" + filename + ".jpg"

# RUn command
os.system("adb pull \"" + filepath + "\" .")
