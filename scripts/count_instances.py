import sys
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import itertools

def main():
	file_dir = "../../../data/handshakes/old"
	instances = [Instance(f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
	ids = [f.time for f in instances]
	print sum(i.device == "f1e9aab2b6d848c4" for i in instances)
	print sum(i.device == "54c4157b18204380" for i in instances)
	print sum(i.name != None for i in instances)
	id_combinations = [i for i in itertools.combinations(instances, 2)]
	print len(id_combinations)
	id_combinations_device_filtered = [i for i in id_combinations if i[0].device != i[1].device]
	print len(id_combinations_device_filtered)

	filtered = [i for i in id_combinations if abs(int(i[0].time) - int(i[1].time)) == 3]

	for idx, i in enumerate(filtered):
		print idx
		print (i[0].time, i[1].time)
		print (i[0].name, i[1].name)
		print (i[0].device, i[1].device)
		print

class Instance:
	def __init__(self, f):
		split = f.split('.')[0].split('-')
		self.time = split[0]
		self.device = split[1]
		if len(split) > 2:
			self.name = split[2].strip().lower()
		else:
			self.name =None

if __name__ == '__main__':
	main()