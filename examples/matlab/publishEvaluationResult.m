function publishEvaluationResult(result, chromosome)

requestBody.fitness = result;
requestBody.evaluationId = chromosome.evaluationId;
request = matlab.net.http.RequestMessage('POST', [], requestBody);
[result, completedResult, ~] = send(request,'localhost:8080/chromosome/'+string(chromosome.id)+'/evaluationResult');

end

