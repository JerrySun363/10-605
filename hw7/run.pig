-- load data
SET default_parallel 10;

--load raw train data, including events and labels
rawData = LOAD '/user/chens1/train/rcv1_train_events/' AS (xy:chararray,c:int);
cleanData =  FOREACH rawData GENERATE STRSPLIT(xy, ',') AS xy:(x:chararray,y:chararray), c as c;

--dump cleanData;

--now we calculate vocSize
wordTotal = GROUP cleanData by xy.x;
vocabulary = foreach wordTotal generate group as word; 
voc = GROUP vocabulary all;--total vocabulary size
vocSize = FOREACH voc GENERATE COUNT(vocabulary) as total;

--filter the label we want
ECATdata = FILTER cleanData BY xy.y == 'lab=ECAT';

ECATgroup = GROUP ECATdata all;
totalCount = foreach ECATgroup GENERATE SUM(ECATdata.c) as total; 
--total count of words
temp1 = CROSS vocSize , totalCount;
totalFenmu = FOREACH temp1 GENERATE (vocSize.total +totalCount.total) as sum;

--generate words for future use.
ECATdataFormal = FOREACH ECATdata Generate SUBSTRING(xy.x, 5, (int)SIZE(xy.x)) as word, c as c;
--now load test data.
classData = LOAD '/user/chens1/train/rcv1_train_class_events/' as (x:chararray, c:int);
classTotal1 = GROUP classData all;
classTotal = FOREACH classTotal1 GENERATE SUM(classData.c) as classTotalSum;

--the following is to use.

classF = FILTER classData BY x == 'lab=ECAT';
classInUse = CROSS classF, classTotal;

--no smooth for class event
classPrior =  FOREACH classInUse Generate classF.x as label, LOG((classF.c* 1.0)/(classTotal.classTotalSum * 1.0 )) as prior;

--load test data
rawTestData = Load '/user/chens1/train/rcv1_test/small/' as (x:int,labels:chararray, words:chararray);
testData1 = FOREACH rawTestData GENERATE x as x, labels as labels, FLATTEN(TOKENIZE(words)) as word;
testData = Filter testData1 by word MATCHES '\\w+';

--testData  = FOREACH testdata2 Generate x as x, labels as labels, SUBSTRING(word, 5, (int)SIZE(word)) as word;
joinTable1 = JOIN testData BY word LEFT OUTER, ECATdataFormal by word; 
joinTable2  = FOREACH joinTable1 Generate testData::x as x, testData::word as word, (ECATdataFormal::c is null ? 1 : (ECATdataFormal::c+1)) as count;

joinTable = Cross joinTable2, totalFenmu;
result = FOREACH joinTable Generate joinTable2::x as x,  joinTable2::word as word, LOG(joinTable2::count * 1.0 /totalFenmu::sum) as prob;

resultGroup = GROUP result by x;
resultSum =  FOREACH resultGroup GENERATE group as x, SUM(result.prob) as totalProb;
resultFinal1 = Cross resultSum, classPrior;
--
resultFinal2 = FOREACH resultFinal1 Generate resultSum::x as x , (resultSum::totalProb + classPrior::prior) as totalProb;
resultFinal3 = JOIN resultFinal2 by x, rawTestData by x;

resultFinal4  = FOREACH resultFinal3 Generate rawTestData::x as x , rawTestData::labels as labels, resultFinal2::totalProb as prob;  
resultFinal5 = ORDER resultFinal4 BY prob DESC;
resultFinal = LIMIT resultFinal5 1000;
--store resultFinal into 's3://chensun/output3/';

store resultFinal into '/user/chens1/small';
