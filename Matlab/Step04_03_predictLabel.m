function Step04_03_predictLabel()
clear;clc;
isSelfEvalute = 1; %1:从标注数据中抽出1/3进行验证，0:直接对测试数据进行标注
selectFeatureSet = 0; %是否选择特征，1 进行选择直到找到最大值(此时isSelfEvalute必须为1)，0 不进行选择
method = 'SVM'; % RF(Random Forest), SVM, MLR(MultiNomial logistic Regression)
isKernel = 0; % 1:SVM是否使用Gaussian高斯核，0: SVM采用线性Linear
hasLoadedData = 0; %0: 还没有加载过数据，1: 已经加载过数据了
useFullData = 1; %0: 使用Abstract特征，1: 使用Abstract+InfoBox属性特征

if ~hasLoadedData
    % 先加载特征
    load('./../RefineData/Step02_06_Train_full.VSM');
    load('./../RefineData/Step02_06_Train_abstract.VSM');
    load('./../RefineData/Step02_06_Test_full.VSM');
    load('./../RefineData/Step02_06_Test_abstract.VSM');
    train_full = Step02_06_Train_full;
    train_abstract = Step02_06_Train_abstract;
    test_full = Step02_06_Test_full;
    test_abstract = Step02_06_Test_abstract;
    % 加载标签
    load('./../RefineData/Step02_01_train_label.dat');
    train_label = Step02_01_train_label;
    clear Step02_*;
    save './../RefineData/Step03_01_all_Fea.mat' train_* test_*
else
    load('./../RefineData/Step03_01_all_Fea.mat')
end
parameters.method = method;
parameters.isKernel = isKernel;
parameters.isSelfEvalute = isSelfEvalute;

%% 提出验证集合
iniIndx = 3;
if useFullData
    labeled_fea = train_full;
    unlabeled_fea = test_full;
    train_labels = train_label;
else
    labeled_fea = train_abstract;
    unlabeled_fea = test_abstract;
    train_labels = train_label;
end
if isSelfEvalute
    % 如果是自己测试的话，则从训练集合中抽出一部分作为测试数据
    unlabeled_fea = labeled_fea(iniIndx:3:1397 ,:);
    test_labels = train_labels(iniIndx:3:1397,:);
    labeled_fea(iniIndx:3:1397 ,:) = [];
    train_labels(iniIndx:3:1397 ,:) = [];
else
    test_labels = zeros(length(unlabeled_fea(:,1)),1);
end

%% 利用VSM输入得到训练数据和测试数据的TF-IDF
disp('Compute TF-IDF')
[labeled_fea, unlabeled_fea] ...
          = tf_idf(labeled_fea, unlabeled_fea);

%% 开始进行特征选择，如果需要的话，不需要的话则直接模型训练
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
    %先进行全特征评估
    curr_best_feature_index = find(best_feature_set==1);
    parameters.curr_best_feature_index = curr_best_feature_index;
    [~,hist_topF1_score] = evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, parameters);
    disp(['The init AUC score is:',num2str(hist_topF1_score),'!']);
    % 如果特征维数是m,那么最多选择m-1轮选择
    for iter_i=1:(feature_dim-1);
        %开始遍历剩下的特征集合，每次遍历找到一个剔除后得分最高的特征
        %跟上次得分进行对比，如果得分升高了则进行剔除，如果得分下降了直接终止循环
        curr_best_feature_index = find(best_feature_set==1);
        curr_best_feature_set_size = length(curr_best_feature_index);
        disp(['Current feature_dim is:',num2str(curr_best_feature_set_size)]);
        for fea_i=1:curr_best_feature_set_size;
            parameters.curr_best_feature_index = curr_best_feature_index;
            parameters.curr_best_feature_index(fea_i) = [];
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
           %% 把利用特征计算验证得分做成函数
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
            %剔除噪音特征
            disp(['The ',num2str(iter_i),' th rid index is:',num2str(curr_topAUC_ridIndex),'!']);
            best_feature_set(curr_topAUC_ridIndex)=0;
        end
        curr_F1_score=[];
    end
else
    %% 不进行特征选择直接计算
    [curr_ACC,curr_F1_score] = evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, parameters);
end
end



function [ACC, F1_score]=evaluateMode(labeled_fea, train_labels, unlabeled_fea, test_labels, parameters)
F1_score = 0;
method = parameters.method;
isKernel = parameters.isKernel;
isSelfEvalute = parameters.isSelfEvalute;
curr_best_feature_index = parameters.curr_best_feature_index;
labeled_fea = labeled_fea(:,curr_best_feature_index);
unlabeled_fea = unlabeled_fea(:,curr_best_feature_index);
if (strcmp(method,'SVM'))
%% 进行SVM模型训练和预测
    labeled_fea = sparse(labeled_fea);
    unlabeled_fea = sparse(unlabeled_fea);
    tic
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
    toc
    if isSelfEvalute
        ACC = length(find(predict_label == test_labels))/length(test_labels)*100;
        if ~isKernel
            disp(['[SVM-Linear] Accuracy is ',num2str(ACC)])
            disp(['[SVM-Linear] The area under the ROC curve (AUC) is ',num2str(ACC)])
        else
            disp(['[SVM-RBF] Accuracy is ',num2str(ACC)])
            disp(['[SVM-RBF] The area under the ROC curve (AUC) is ',num2str(ACC)])
        end
    else
       save './../predictSVM_label.txt' predict_label -ascii
    end

elseif (strcmp(method,'RF'))
    %% 进行随机森林分类器（Random Forest）训练和预测
    labeled_fea = full(labeled_fea);
    unlabeled_fea = full(unlabeled_fea);
    nTree = 100;
    tic
    disp('start train Random Forest model ...')
    model = TreeBagger(nTree, labeled_fea, train_labels);
    disp('start predict test data via Random Forest model ...')
    [predict_label,predict_scores] = predict(model, unlabeled_fea);
    predict_label = str2num(cell2mat(predict_label));
    toc
    if isSelfEvalute
        ACC = length(find(predict_label == test_labels))/length(test_labels)*100;
        disp(['[Random Forest] Accuracy is ',num2str(ACC)])
%         F1_score = scoreF1(test_labels, predict_label);
        disp(['[Random Forest] The area under the ROC curve (AUC) is ',num2str(ACC)])
    else
       save './../predictRandomForest_label.txt' predict_label -ascii
    end

elseif (strcmp(method,'MLR'))
    %% 进行逻辑回归(多项式MultiNomial logistic Regression)训练和预测
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

