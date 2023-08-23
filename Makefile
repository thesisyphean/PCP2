# PCP2 Makefile

SRC_DIR := src
BIN_DIR := bin

# Names of the .java files
CLASSES = Clubgoer ClubGrid ClubSimulation ClubView \
		  CounterDisplay GridBlock PeopleCounter PeopleLocation

# The .class files
CLASSES_FILES = $(CLASSES:%=$(BIN_DIR)/%.class)

# By default, everything is compiled
all: $(CLASSES_FILES)

# Other classes ---
$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	javac -d $(BIN_DIR) -sourcepath $(SRC_DIR) -cp $(BIN_DIR) $<

run: $(CLASSES_FILES)
	java -cp bin ClubSimulation

clean:
	rm $(BIN_DIR)/*.class
