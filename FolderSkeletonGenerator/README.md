# Folder Skeleton Generator
Basically i needed to re-order my folders, but that's boring so i pulled this out of my ass.
in my opinion the coding is some of the best i've done, probably a few things here and there i could touch up a bit (the variable fds for example)
anyways enjoy. if you don't want to build it yourself, the .exe you need to run is in bin\Release\netcoreapp3.1

# Usage
FSG --file <filename> <br />
the file you are targetting should be structured like a tree, the parent should be at the top, blah blah blah i cant describe it myself so please go look at the example i left there. <br />
random rules of the skeleton file: Tabs means next child. No spaces or other characters. I don't know if it works with spaces yet so let's just leave that alone. i had it throw an error before because there were tabs after one of the lines, but i dont know how that will go with actually creating directories so shutup if it deletes your OS. it doesnt handle empty lines either. wow writing this all out i really did a shit job of foolproofing it.
