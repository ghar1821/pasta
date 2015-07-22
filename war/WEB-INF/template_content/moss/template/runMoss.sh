defaultLocation=$1
assessment=$2

parameter=$(./scan.sh $defaultLocation $assessment)

perl moss -l java -d $parameter > out.txt

tail -1 out.txt > location.txt
