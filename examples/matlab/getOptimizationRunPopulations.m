function [optimizationRunPopulationsPage] = getOptimizationRunPopulations(optimizationRunId, page)

if ~exist('page','var')
    page = 1;
end

request = matlab.net.http.RequestMessage('GET');
[response, completedResponse, ~] = send(request, 'localhost:8080/optimizationRun/'+string(optimizationRunId)+'/populations?page='+string(page));

if(completedResponse.Completed)
    optimizationRunPopulationsPage = response.Body.Data;
end

end

