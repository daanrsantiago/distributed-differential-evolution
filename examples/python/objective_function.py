import scipy.integrate as integrate
from scipy.integrate import ode
from model import model

def objective_function(x) -> float:
    time_initial = 0
    time_final = 2
    control = [0.0, 9.0, 0.0, 0.0, 9.0, 0.0]
    solution = integrate.solve_ivp(
        fun=model,
        t_span=[time_initial, time_final], 
        y0=[5.0, 0.0, 10.0, 0.0, 0.0],
        args=(x, control),
        dense_output=True,
        method='RK45'
    )

    result = integrate.RK45(
        fun,
        t0=time_initial,
        y0=[5.0, 0.0, 10.0, 0.0, 0.0],
        t_bound=time_final
    )
    
    times = [result.t]
    ys = [result.y]

    while result.status == 'running':
        result.step()  # Avançar um passo
        times.append(result.t)
        ys.append(result.y)

    # result = ode(model)
    # result.set_integrator('vode', method='bdf', with_jacobian=False)
    # result.set_initial_value([5.0, 0.0, 10.0, 0.0, 0.0], time_initial)
    # result.set_f_params(x, control)
    # dt = 0.0001
    # while result.successful() and result.t < time_final: 
    #     result.integrate(result.t + dt)

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

def fun(t,y):
    return model(t,y, [0.6319, 1.5280, 0.6270, 1.5622], [0.0, 9.0, 0.0, 0.0, 9.0, 0.0])