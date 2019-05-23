from manager import *
import sys

if __name__ == "__main__":
	
	m = Manager(sys.argv)
	m.manage_input()
	m.pipeline()
	m.show()
