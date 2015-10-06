#!/bin/bash

if [ $(/usr/bin/whoami) != "ldpdapp" ]
then
  echo "ERROR: Script be run as user ldpdapp!"
  exit
fi

# Bring in $archiveit_user, $archiveit_password, $archiveit_collection_id and $archive_download_directory variables
source ./getarcs-config

base_run_directory=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
checksum_filename='arc-md5sums.txt'

RUN_MODE=$1
if [ "$RUN_MODE" == 'download' ]; then
  echo 'Run mode: download'

  # Keep previous copy of md5 checksum file
  if [ -e "$checksum_filename" ]; then
    mv "$checksum_filename" "$checksum_filename.previous"
    echo "Backed up previous checksum file to $checksum_filename.previous"
  fi

  # Get latest copy of checksum file
  wget --http-user=$archiveit_username --http-password=$archiveit_password --accept gz,txt https://partner.archive-it.org/cgi-bin/getarcs.pl/md5sums.txt?coll=$archiveit_collection_id -O "$checksum_filename"
elif [ "$RUN_MODE" == 'validate' ]; then
    echo 'Run mode: validate'
else
    echo "Usage: ./getarcs.sh [download|validate]"
    exit
fi

cd $base_run_directory

checksum_file="$base_run_directory/$checksum_filename"
counter=1
total=`cat $checksum_file | wc -l`

declare -a missing_files_to_download
declare -a files_that_failed_validation

while read line; do
  HASH=$( echo "$line" | cut -f1 -d' ' )
  HASH=$( echo $HASH | sed -e 's/^ *//' -e 's/ *$//') # Trim leading and trailing whitespace

  HASH_LENGTH=${#HASH}
  FILE_PATH=${line:HASH_LENGTH}
  FILE_PATH=$( echo $FILE_PATH | sed -e 's/^ *//' -e 's/ *$//') # Trim leading and trailing whitespace

  if [ -a "$archive_download_directory/$FILE_PATH" ]; then
    if [ "$RUN_MODE" == 'validate' ]; then
      NEWLY_CALCULATED_HASH=`md5sum "$archive_download_directory/$FILE_PATH"`
      # Remove file path from sum output
      NEWLY_CALCULATED_HASH=$( echo "$NEWLY_CALCULATED_HASH" | cut -f1 -d' ' )
      # Trim leading and trailing whitespace
      NEWLY_CALCULATED_HASH=$( echo $NEWLY_CALCULATED_HASH | sed -e 's/^ *//' -e 's/ *$//')    

      if [[ "$HASH" != "$NEWLY_CALCULATED_HASH" ]]; then
        files_that_failed_validation+=($FILE_PATH)
        echo "Validation failed for $FILE_PATH"
      fi
    fi
  else
    missing_files_to_download+=($FILE_PATH)
    if [ "$RUN_MODE" == 'download' ]; then
      # Perform download
      wget --http-user=$archiveit_username --http-password=$archiveit_password https://partner.archive-it.org/cgi-bin/getarcs.pl/$FILE_PATH -O "$archive_download_directory/$FILE_PATH"
    fi
  fi

  echo "Processed $counter of $total"
  counter=$((counter+1))

done < $checksum_file

if [ "$RUN_MODE" == 'validate' ]; then

  # Print failed validations to log

  NUM_FAILED_VALIDATIONS=${#files_that_failed_validation[@]}
  ERROR_FILE=getarcs-validation.err.$(date +"%Y-%m-%d%H%M%S")

  echo "NUM_FAILED_VALIDATIONS: $NUM_FAILED_VALIDATIONS"

  if (( "$NUM_FAILED_VALIDATIONS" > "0" )); then
    echo "(W)ARC validation failed. Number of files that failed validation: $NUM_FAILED_VALIDATIONS"
    echo "Failed validations have been written to file: $ERROR_FILE"
    printf "%s\n" "${files_that_failed_validation[@]}" > $ERROR_FILE
  else
    echo "(W)ARC files passed checksum validation."
  fi

fi

echo "Done."