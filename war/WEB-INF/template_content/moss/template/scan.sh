userList=$(ls $1)
parameters=""

for user in $userList
do
	submissionCount=$(ls $1/$user/assessments/$2/ -1 | wc -l)
	if [ $submissionCount -gt 0 ]; then
		parameters="$parameters "$1/$user/assessments/$2/$(ls $1/$user/assessments/$2/ | tail -1)/submission/
	fi
done

echo $parameters
