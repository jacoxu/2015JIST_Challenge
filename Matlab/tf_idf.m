function [trainX, testX] = tf_idf(trainTF, testTF)
% TF-IDF weighting
% ([1+log(tf)]*log[N/df])
%��ȡ����VSM�����ռ�ģ�͵�ά�������õ��ĵ�����term��
[n,m] = size(trainTF);  % the number of (training) documents and terms
%ͳ��ѵ������VSM��Term���ĵ�Ƶ��
df = sum(trainTF>0);  % (training) document frequency
%ֻ��ȡTermƵ�δ���0�����ݣ�������ѵ���ĵ��д�δ���ֹ��Ĵ�
d = sum(df>0); % the number of dimensions, i.e., terms occurred in (training) documents
%���ĵ�Ƶ��df���������򣬵õ������Ľ��dfY�����кŴ���dfI
[dfY, dfI] = sort(df, 2, 'descend');
%��ѵ������VSM�й��˵�δ���ִ�
trainTF = trainTF(:,dfI(1:d));
%��Ӧ��Ҳ�Ӳ��Լ��й��˵���Ӧ�Ĵ�
testTF = testTF(:,dfI(1:d));
%����Term��idf����
idf = log(n./dfY(1:d));
%����IDFϡ��������ں���ľ�������
IDF = sparse(1:d,1:d,idf);
%��ѵ����VSM���ҳ�����Ԫ�أ��ֱ�Ϊ���кţ��кź�Ԫ��ֵ
[trainI,trainJ,trainV] = find(trainTF);
%������ѵ�����ݼ�TF-IDFǰ��κ�����ϡ�����
trainLogTF = sparse(trainI,trainJ,1+log(trainV),size(trainTF,1),size(trainTF,2));
%�Ӳ��Լ�VSM���ҳ�����Ԫ�أ��ֱ�Ϊ���кţ��кź�Ԫ��ֵ
[testI,testJ,testV] = find(testTF);
%�����˲������ݼ�TF-IDFǰ��κ�����ϡ�����
testLogTF = sparse(testI,testJ,1+log(testV),size(testTF,1),size(testTF,2));
%����������TF-IDF����
trainX = trainLogTF*IDF;
testX = testLogTF*IDF;

end
