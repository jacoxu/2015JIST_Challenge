function Step03_01_predictLabel()
rand('state',0) %�����Ϊ��Ҫ
randn('state',0) %�����Ϊ��Ҫ
clear;clc;
isSelfEvalute = 0; %1:�ӱ�ע�����г��1/3������֤��0:ֱ�ӶԲ������ݽ��б�ע
iniIndx = 3;
selectFeatureSet = 0; %�Ƿ�ѡ��������1 ����ѡ��ֱ���ҵ����ֵ(��ʱisSelfEvalute����Ϊ1)��0 ������ѡ��
method = 'SVM'; % RF(Random Forest), SVM, MLR(MultiNomial logistic Regression)
isKernel = 0; % 1:SVM�Ƿ�ʹ��Gaussian��˹�ˣ�0: SVM��������Linear
%-1: ʹ��abstract������0: ʹ��full������1: ʹ��FirstSent����, 2:  ʹ��1Gram����, 3:ʹ��full+FirstSent, 
% 4: ʹ��full+1Gram, 5:ʹ��First+1Gram, 6: ʹ��full+FirstSent+1Gram
% 7: ʹ��full+FirstSent+1Gram+FirstSubSent
% 8: ʹ��full+FirstSent+1Gram+Sub1gram
% 9: ʹ��full+FirstSent+1Gram+FirstSubSent+Sub1gram
% 10: ʹ��full+FirstSubSent+Sub1gram
useFullData = 6;
hasPostProcess = 1; %�Ƿ���к���
needSelfTraining = 1; %0�Ƿ���Ҫ��ѵ����ලѧϰ���������Լ������ݼ��뵽ѵ�����н���ѵ��
hasLoadedData = 1; %0: ��û�м��ع����ݣ�1: �Ѿ����ع�������
hasTestTrueLabel = 1; %����õ��˲��Լ�����ʵ��ǩ

if ~hasLoadedData
    % �ȼ�������
    load('./../RefineData/Step04_03_Train_full.VSM');
    load('./../RefineData/Step04_03_Test_full.VSM');
    train_full = Step04_03_Train_fea_full;
    test_full = Step04_03_Test_fea_full;
    
    load('./../RefineData/Step04_03_Train_fea_abstract.VSM');
    load('./../RefineData/Step04_03_Test_fea_abstract.VSM');
    train_abstract = Step04_03_Train_fea_abstract;
    test_abstract = Step04_03_Test_fea_abstract;
    
    load('./../RefineData/Step04_03_Train_fea_firstSent_plus.VSM');
    load('./../RefineData/Step04_03_Test_fea_firstSent_plus.VSM');
    train_firstSent_plus = Step04_03_Train_fea_firstSent_plus;
    test_firstSent_plus = Step04_03_Test_fea_firstSent_plus;
    
    load('./../RefineData/Step04_03_Train_fea_1gram_plus.VSM');
    load('./../RefineData/Step04_03_Test_fea_1gram_plus.VSM');
    train_1gram_plus = Step04_03_Train_fea_1gram_plus;
    test_1gram_plus = Step04_03_Test_fea_1gram_plus;
    
    load('./../RefineData/Step04_03_Train_fea_firstSubSent_plus.VSM');
    load('./../RefineData/Step04_03_Test_fea_firstSubSent_plus.VSM');
    train_firstSubSent_plus = Step04_03_Train_fea_firstSubSent_plus;
    test_firstSubSent_plus = Step04_03_Test_fea_firstSubSent_plus;
    
    load('./../RefineData/Step04_03_Train_fea_Sub1gram_plus.VSM');
    load('./../RefineData/Step04_03_Test_fea_Sub1gram_plus.VSM');
    train_Sub1gram_plus = Step04_03_Train_fea_Sub1gram_plus;
    test_Sub1gram_plus = Step04_03_Test_fea_Sub1gram_plus;
    
    load('./../RefineData/Step04_03_Train_fea_keyword_plus.dat');
    load('./../RefineData/Step04_03_Test_fea_keyword_plus.dat');    
    train_keyword_plus = Step04_03_Train_fea_keyword_plus;
    test_keyword_plus = Step04_03_Test_fea_keyword_plus;
    
    load('./../RefineData/Step04_03_Train_fea_secKeyword_plus.dat');
    load('./../RefineData/Step04_03_Test_fea_secKeyword_plus.dat');    
    train_secKeyword_plus = Step04_03_Train_fea_secKeyword_plus;
    test_secKeyword_plus = Step04_03_Test_fea_secKeyword_plus;     
    
    load('./../RefineData/Step04_03_Train_fea_area_plus.dat');
    load('./../RefineData/Step04_03_Test_fea_area_plus.dat');    
    train_area_plus = Step04_03_Train_fea_area_plus;
    test_area_plus = Step04_03_Test_fea_area_plus;       
    % ���ر�ǩ
    load('./../RefineData/Step02_01_train_label.dat');
    train_label = Step02_01_train_label;
    clear Step0*;
    save './../RefineData/Step04_03_all_Fea.mat' train_* test_*
else
    load('./../RefineData/Step04_03_all_Fea.mat')
end
if hasTestTrueLabel
    load('./../RefineData/Step02_01_test_label.dat')
    test_labels = Step02_01_test_label;
end
parameters.method = method;
parameters.isKernel = isKernel;
parameters.isSelfEvalute = isSelfEvalute;
parameters.hasPostProcess = hasPostProcess;
parameters.needSelfTraining = needSelfTraining;
parameters.hasTestTrueLabel = hasTestTrueLabel;
parameters.isSecondIterTrain = 0;
%% �����֤����
if (useFullData==-1)
    labeled_fea = train_abstract;
    unlabeled_fea = test_abstract;
elseif (useFullData==0)
    labeled_fea = train_full;
    unlabeled_fea = test_full;
elseif(useFullData==1)
    labeled_fea = train_firstSent_plus;
    unlabeled_fea = test_firstSent_plus;
elseif(useFullData==2)
    labeled_fea = train_1gram_plus;
    unlabeled_fea = test_1gram_plus;
elseif(useFullData==3)
    labeled_fea = train_full+train_firstSent_plus;
    unlabeled_fea = test_full+test_firstSent_plus;
elseif(useFullData==4)
    labeled_fea = train_full+train_1gram_plus;
    unlabeled_fea = test_full+test_1gram_plus;  
elseif(useFullData==5)
    labeled_fea = train_firstSent_plus+train_1gram_plus;
    unlabeled_fea = test_firstSent_plus+test_1gram_plus;  
elseif(useFullData==6)
    labeled_fea = train_full+train_firstSent_plus+train_1gram_plus;
    unlabeled_fea = test_full+test_firstSent_plus+test_1gram_plus;
elseif(useFullData==7)
    labeled_fea = train_full+train_firstSent_plus+train_1gram_plus+train_firstSubSent_plus;
    unlabeled_fea = test_full+test_firstSent_plus+test_1gram_plus+test_firstSubSent_plus;
elseif(useFullData==8)
    labeled_fea = train_full+train_firstSent_plus+train_1gram_plus+train_Sub1gram_plus;
    unlabeled_fea = test_full+test_firstSent_plus+test_1gram_plus+test_Sub1gram_plus;    
elseif(useFullData==9)
    labeled_fea = train_full+train_firstSent_plus+train_1gram_plus+train_firstSubSent_plus+train_Sub1gram_plus;
    unlabeled_fea = test_full+test_firstSent_plus+test_1gram_plus+test_firstSubSent_plus+test_Sub1gram_plus;
elseif(useFullData==10)
    labeled_fea = train_full+train_firstSubSent_plus+train_Sub1gram_plus;
    unlabeled_fea = test_full+test_firstSubSent_plus+test_Sub1gram_plus;    
else
    disp(['Wrong useFullData',num2str(useFullData)]);
end
train_labels = train_label;
if isSelfEvalute
    % ������Լ����ԵĻ������ѵ�������г��һ������Ϊ��������
    unlabeled_fea = labeled_fea(iniIndx:3:1397 ,:);
    test_labels = train_labels(iniIndx:3:1397,:);
    test_keyword_plus = train_keyword_plus(iniIndx:3:1397,:);
    test_secKeyword_plus = train_secKeyword_plus(iniIndx:3:1397,:);   
    test_area_plus = train_area_plus(iniIndx:3:1397,:);    
    labeled_fea(iniIndx:3:1397 ,:) = [];
    train_labels(iniIndx:3:1397 ,:) = [];
    train_keyword_plus(iniIndx:3:1397,:) = [];
    train_secKeyword_plus(iniIndx:3:1397,:) = [];
    train_area_plus(iniIndx:3:1397,:) = [];
elseif ~hasTestTrueLabel
    test_labels = zeros(length(unlabeled_fea(:,1)),1);
end

%% ����VSM����õ�ѵ�����ݺͲ������ݵ�TF-IDF
disp('Compute TF-IDF')
[labeled_fea, unlabeled_fea] ...
          = tf_idf(labeled_fea, unlabeled_fea);

%% ��ʼ��������ѡ�������Ҫ�Ļ�������Ҫ�Ļ���ֱ��ģ��ѵ��
feature_dim = length(labeled_fea(1,:));
disp(['The init feature_dim is:',num2str(feature_dim)]);
best_feature_set = ones(1,feature_dim);
curr_best_feature_index = find(best_feature_set==1);
parameters.curr_best_feature_index = curr_best_feature_index;
if (isSelfEvalute && selectFeatureSet)
    hist_topF1_score = 0;
    curr_topF1_score = 0;
    curr_topAUC_ridIndex = 0;
    curr_F1_score = [];
    %�Ƚ���ȫ��������
    curr_best_feature_index = find(best_feature_set==1);
    parameters.curr_best_feature_index = curr_best_feature_index;
    [~,hist_topF1_score] = evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, parameters);
    disp(['The init AUC score is:',num2str(hist_topF1_score),'!']);
    % �������ά����m,��ô���ѡ��m-1��ѡ��
    for iter_i=1:(feature_dim-1);
        %��ʼ����ʣ�µ��������ϣ�ÿ�α����ҵ�һ���޳���÷���ߵ�����
        %���ϴε÷ֽ��жԱȣ�����÷�������������޳�������÷��½���ֱ����ֹѭ��
        curr_best_feature_index = find(best_feature_set==1);
        curr_best_feature_set_size = length(curr_best_feature_index);
        disp(['Current feature_dim is:',num2str(curr_best_feature_set_size)]);
        for fea_i=1:curr_best_feature_set_size;
            parameters.curr_best_feature_index = curr_best_feature_index;
            parameters.curr_best_feature_index(fea_i) = [];
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
           %% ����������������֤�÷����ɺ���
            [~,curr_F1_score(fea_i)] = evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, parameters);
        end
        [curr_topF1_score, curr_topAUC_ridIndex] = max(curr_F1_score);
        disp(['Current AUC score is:',num2str(curr_topF1_score),'!']);
        if curr_topF1_score <= hist_topF1_score
            disp(['Has find the best feature sets:[',num2str(best_feature_set),'].']);
            disp(['And the best AUC score is:',num2str(hist_topF1_score),'!']);
            disp('End iterator!');
            break;
        else
            hist_topF1_score = curr_topF1_score;
            %�޳���������
            disp(['The ',num2str(iter_i),' th rid index is:',num2str(curr_topAUC_ridIndex),'!']);
            best_feature_set(curr_topAUC_ridIndex)=0;
        end
        curr_F1_score=[];
    end
else
    %% ����������ѡ��ֱ�Ӽ���
    tic
    [curr_ACC,curr_F1_score] = evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, test_keyword_plus, test_secKeyword_plus, test_area_plus, parameters);
    toc
end
end

function [ACC, F1_score]=evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, test_keyword_plus, test_secKeyword_plus, test_area_plus, parameters)
F1_score = 0;
method = parameters.method;
isKernel = parameters.isKernel;
isSelfEvalute = parameters.isSelfEvalute;
curr_best_feature_index = parameters.curr_best_feature_index;
labeled_fea = labeled_fea(:,curr_best_feature_index);
unlabeled_fea = unlabeled_fea(:,curr_best_feature_index);
if (strcmp(method,'SVM'))
%% ����SVMģ��ѵ����Ԥ��
    labeled_fea = sparse(labeled_fea);
    unlabeled_fea = sparse(unlabeled_fea);
    y = zeros(length(unlabeled_fea(:,1)),1);
    if ~isKernel
        disp('start train linear SVM model ...')
        model = train(train_labels, labeled_fea, '-q');
        disp('start predict test data via linear SVM ...')
        [predict_label, accuracy, predict_scores] = predict(y,unlabeled_fea,model,'-b 1');
    else
        disp('start train kernel SVM model ...')
        model = svmtrain(train_labels,labeled_fea,['-q -c 1 -g ',int2str(length(labeled_fea))]);
        disp('start predict test data via kernel SVM model ...')
        [predict_label, accuracy, predict_scores] = svmpredict(y,unlabeled_fea,model);
    end
    [label,rOrder] = sort(model.Label);
    predict_scores = predict_scores(:,rOrder);
    predict_scores = exp(predict_scores);
    predict_scores = bsxfun(@rdivide,predict_scores,sum(predict_scores,2));
    [~,predict_label] = max(predict_scores');
    predict_label = predict_label'-1;
    if (parameters.isSelfEvalute||parameters.hasTestTrueLabel)
        ACC = length(find(predict_label == test_labels))/length(test_labels)*100;
        F1_score = f1Measure(predict_label, test_labels);
        if ~isKernel
            disp(['[SVM-Linear] Accuracy is ',num2str(ACC)])
            disp(['[SVM-Linear] F1 is ',num2str(F1_score)])
        else
            disp(['[SVM-RBF] Accuracy is ',num2str(ACC)])
            disp(['[SVM-RBF] F1 is ',num2str(F1_score)])
        end
        if parameters.hasPostProcess
            wight1 = 0.1;
            wight2 = 0;
            disp(['PostPro wight is:',num2str(wight1)]);
            predict_scores = predict_scores + wight1.*test_keyword_plus + wight2.*test_secKeyword_plus + test_area_plus;
            [~,predict_label] = max(predict_scores');
            predict_label = predict_label'-1;
            ACC = length(find(predict_label == test_labels))/length(test_labels)*100;
            F1_score = f1Measure(predict_label, test_labels);
            if ~isKernel
                disp(['[SVM-Linear+PostPro] Accuracy is ',num2str(ACC)])
                disp(['[SVM-Linear+PostPro] F1 is ',num2str(F1_score)])
            else
                disp(['[SVM-RBF+PostPro] Accuracy is ',num2str(ACC)])
                disp(['[SVM-RBF+PostPro] F1 is ',num2str(F1_score)])
            end
        end
        if parameters.isSecondIterTrain
            %% ѵ�����ɭ�ֽ��е���
              
        end
        if parameters.needSelfTraining
            for nlimit = 0.2 %0.1:0.05:0.5
                [max_scores, max_indexs] = max(predict_scores');
                for j=1:length(max_indexs)
                    predict_scores(j,max_indexs(j)) = 0;                    
                end
                [secmax_scores, secmax_indexs] = max(predict_scores');
                selfLabelIndex = find((max_scores-secmax_scores)>nlimit);
                labeled_fea = [labeled_fea; unlabeled_fea(selfLabelIndex,:)];
                train_labels = [train_labels;predict_label(selfLabelIndex)];              
                parameters.needSelfTraining = 0;
                parameters.isSecondIterTrain = 1;
                [curr_ACC,curr_F1_score] = evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, test_keyword_plus, test_secKeyword_plus, test_area_plus, parameters);
            end
        end
    end
    if ~parameters.isSelfEvalute
       save './../predictSVM_label.txt' predict_label -ascii
    end

elseif (strcmp(method,'RF'))
    %% �������ɭ�ַ�������Random Forest��ѵ����Ԥ��
    labeled_fea = full(labeled_fea);
    unlabeled_fea = full(unlabeled_fea);
    nTree = 1000;
    disp('start train Random Forest model ...')
    model = TreeBagger(nTree, labeled_fea, train_labels);
    disp('start predict test data via Random Forest model ...')
    [predict_label,predict_scores] = predict(model, unlabeled_fea);
    predict_label = str2num(cell2mat(predict_label));
    toc
    if (parameters.isSelfEvalute||parameters.hasTestTrueLabel)
        ACC = length(find(predict_label == test_labels))/length(test_labels)*100;
        F1_score = f1Measure(predict_label, test_labels);
        if ~isKernel
            disp(['[Random Forest] Accuracy is ',num2str(ACC)])
            disp(['[Random Forest] F1 is ',num2str(F1_score)])
        else
            disp(['[Random Forest] Accuracy is ',num2str(ACC)])
            disp(['[Random Forest] F1 is ',num2str(F1_score)])
        end
        if parameters.hasPostProcess
            wight = 0.1;
            disp(['PostPro wight is:',num2str(wight)]);
            predict_scores = predict_scores + wight.*test_keyword_plus;
            [~,predict_label] = max(predict_scores');
            predict_label = predict_label'-1;
            ACC = length(find(predict_label == test_labels))/length(test_labels)*100;
            F1_score = f1Measure(predict_label, test_labels);
            disp(['[Random Forest+PostPro] Accuracy is ',num2str(ACC)])
            disp(['[Random Forest+PostPro] F1 is ',num2str(F1_score)])
        end
    end
    if ~parameters.isSelfEvalute
       save './../predictRandomForest_label.txt' predict_label -ascii
    end

elseif (strcmp(method,'MLR'))
    %% �����߼��ع�(����ʽMultiNomial logistic Regression)ѵ����Ԥ��
    labeled_fea = full(labeled_fea);
    unlabeled_fea = full(unlabeled_fea);
    tic
    disp('start train model ...')
    train_labels = train_labels+1;
    model = mnrfit(labeled_fea, train_labels);
    disp('start predict test data ...')
    predict_scores = mnrval(model, unlabeled_fea);
    train_labels = train_labels -1;
    predict_label_p = predict_scores(:,1)*0+predict_scores(:,2)*1+predict_scores(:,3)*2+predict_scores(:,4)*3+...
                      predict_scores(:,5)*4+predict_scores(:,6)*5+predict_scores(:,7)*6+predict_scores(:,8)*7+...
                      predict_scores(:,9)*8+predict_scores(:,10)*9;
    toc
    if isSelfEvalute
        ACC = length(find(predict_label_p == test_labels))/length(test_labels)*100;
        disp(['[MultiNomial LR] Accuracy is ',num2str(ACC)])
        save ./../evaluteScore_MLR.mat evaluteScore;
    else
        sampleSubmission(:,2) = predict_label_p;
        save './../predictMultiNomialLR_labelP.txt' sampleSubmission -ascii
    end
end
end
function F = f1Measure(predict_label, true_labels)
uniqueLabels=unique(true_labels);
a_trueP =[];
b_P =[];
c_true = [];
P_recision = [];
R_ecall = [];
disp(['The length of labels is:',num2str(length(uniqueLabels))]);
for  i=1:1:length(uniqueLabels)
    tmpLabel = uniqueLabels(i);
    tmp_b = (predict_label==tmpLabel);
    b_P(i) = sum(tmp_b);
    tmp_c = (true_labels==tmpLabel);  
    c_true(i) = sum(tmp_c);
    tmp_a = ((tmp_b+tmp_c)==2);
    a_trueP(i) = sum(tmp_a);  
    P_recision(i) = a_trueP(i)/b_P(i);
    R_ecall(i) = a_trueP(i)/c_true(i);
end
P = sum(P_recision);
R = sum(R_ecall);
F = 2*P*R/(P+R);
end

