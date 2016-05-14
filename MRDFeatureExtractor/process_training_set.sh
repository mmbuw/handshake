javac MRDFeatureExtractor.java

java MRDFeatureExtractor training_set/shake1-a.txt 465 feature_data/features-shake1-a.txt
java MRDFeatureExtractor training_set/shake2-a.txt 120 feature_data/features-shake2-a.txt
java MRDFeatureExtractor training_set/shake3-a.txt 150 feature_data/features-shake3-a.txt
java MRDFeatureExtractor training_set/shake4-a.txt 110 feature_data/features-shake4-a.txt
java MRDFeatureExtractor training_set/shake5-a.txt 140 feature_data/features-shake5-a.txt
java MRDFeatureExtractor training_set/shake6-a.txt 140 feature_data/features-shake6-a.txt
java MRDFeatureExtractor training_set/shake7-a.txt 180 feature_data/features-shake7-a.txt

java MRDFeatureExtractor training_set/shake1-t.txt 190 feature_data/features-shake1-t.txt
java MRDFeatureExtractor training_set/shake2-t.txt 250 feature_data/features-shake2-t.txt
java MRDFeatureExtractor training_set/shake3-t.txt 190 feature_data/features-shake3-t.txt
java MRDFeatureExtractor training_set/shake4-t.txt 160 feature_data/features-shake4-t.txt
java MRDFeatureExtractor training_set/shake5-t.txt 180 feature_data/features-shake5-t.txt
java MRDFeatureExtractor training_set/shake6-t.txt 400 feature_data/features-shake6-t.txt
java MRDFeatureExtractor training_set/shake7-t.txt 140 feature_data/features-shake7-t.txt

java MRDFeatureExtractor training_set/no-gestures-mensa.txt -1 feature_data/features-no-gestures-mensa.txt
java MRDFeatureExtractor training_set/no-gestures-dentist.txt -1 feature_data/features-no-gestures-dentist.txt

echo "@RELATION handshake_project_training

@ATTRIBUTE avg_x    REAL
@ATTRIBUTE avg_y    REAL
@ATTRIBUTE avg_z    REAL
@ATTRIBUTE range_x	REAL
@ATTRIBUTE range_y	REAL
@ATTRIBUTE range_z	REAL
@ATTRIBUTE ipd_x	REAL
@ATTRIBUTE ipd_y	REAL
@ATTRIBUTE ipd_z	REAL
@ATTRIBUTE class {no-handshake, handshake}

@DATA" > feature_data/concatenated-features.arff

cat feature_data/*.txt >> feature_data/concatenated-features.arff