import scipy.integrate as integrate
from model import model

def objective_function(x) -> float:
    time_initial = 0
    time_final = 2
    control = [0.0, 9.0, 0.0, 0.0, 9.0, 0.0]
    solution = integrate.solve_ivp(
        fun=model,
        t_span=[time_initial, time_final], 
        y0=[5.0, 0.0, 10.0, 0.0, 0.0],
        args=(x, control)
    )

    resx1 = pow(solution.y[0, -1], 2)
    resx2 = pow(solution.y[1, -1], 2)
    resx3 = pow(solution.y[2, -1], 2)
    resx4 = pow(solution.y[3, -1], 2)
    resx5 = solution.y[-1, 4]

    rp=1000000

    return resx5 + rp*(resx1 + resx2 + resx3 + resx4)

if __name__ == '__main__':
    response_model = model(
        t = 0.75, 
        y = [5.0, 0.0, 10.0, 0.0, 0.0],
        ts = [0.5, 1.0, 1.2, 1.8],
        control = [0.0, 9.0, 0.0, 0.0, 9.0, 0.0]
    )
    print(response_model)