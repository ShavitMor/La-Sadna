import React from 'react';
import '../styles/register.css';
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from 'yup';
import { useForm } from 'react-hook-form';
import { registerMember } from '../API';
import { useNavigate } from 'react-router-dom'; 
import {formatDate}  from '../utils';
import { daDK } from '@mui/x-date-pickers/locales';

const phoneRegExp = /^((\\+[1-9]{1,4}[ \\-]*)|(\\([0-9]{2,3}\\)[ \\-]*)|([0-9]{2,4})[ \\-]*)*?[0-9]{3,4}?[ \\-]*[0-9]{3,4}?$/

const schema = yup.object().shape({
    username: yup.string().required("Username is required"),
    password: yup.string().min(4, "Password must be at least 4 characters").max(20, "Password can't be longer than 20 characters").required("Password is required"),
    confirmPassword: yup.string().oneOf([yup.ref("password"), undefined], "Passwords don't match").required("Confirm Password is required"),
    firstName: yup.string().required("First Name is required"),
    lastName: yup.string().required("Last Name is required"),
    email: yup.string().email("Invalid email format").required("Email is required"),
    phoneNumber: yup.string().matches(phoneRegExp, 'Phone number is not valid').required("Phone number is required"),
    birthDate: yup.date().max(new Date(), "Birth date cannot be in the future").required("Birthday is required"),
    
});

export const Register = () => {
    const { register, handleSubmit, formState: { errors, touchedFields }, trigger } = useForm({
        resolver: yupResolver(schema)
    });
    const navigate = useNavigate();

    const onSubmit = async (data: any) => {
        try {
            data.birthDate = formatDate(new Date(data.birthDate).toISOString());
            const resp = await registerMember(data);
            if (resp.error) {
                alert("Error: " + resp.errorString);
            } else {
                alert("Registered successfully!");
                navigate('/');
            }
        }catch (error) {
            alert("Error ocoured ");
        }
    };

    return (
        <div className="register-container">
            <h2>Register</h2>
            <form onSubmit={handleSubmit(onSubmit)}>
            <div className="form-group">
                    <label htmlFor="username">Username:</label>
                    <input 
                        type="text" 
                        id="username" 
                        placeholder="Enter your username" 
                        {...register("username", { onChange: () => trigger("username") })} 
                        className={errors.username && touchedFields.username ? 'invalid' : ''}
                    />
                    {errors.username && <p className="error-message">{errors.username.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="password">Password:</label>
                    <input 
                        type="password" 
                        id="password" 
                        placeholder="Enter your password" 
                        {...register("password", { onChange: () => trigger("password") })} 
                        className={errors.password && touchedFields.password ? 'invalid' : ''}
                    />
                    {errors.password && <p className="error-message">{errors.password.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="confirmPassword">Confirm Password:</label>
                    <input 
                        type="password" 
                        id="confirmPassword" 
                        placeholder="Confirm your password" 
                        {...register("confirmPassword", { onChange: () => trigger("confirmPassword") })} 
                        className={errors.confirmPassword && touchedFields.confirmPassword ? 'invalid' : ''}
                    />
                    {errors.confirmPassword && <p className="error-message">{errors.confirmPassword.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="firstName">First Name:</label>
                    <input 
                        type="text" 
                        id="firstName" 
                        placeholder="Enter your first name" 
                        {...register("firstName", { onChange: () => trigger("firstName") })} 
                        className={errors.firstName && touchedFields.firstName ? 'invalid' : ''}
                    />
                    {errors.firstName && <p className="error-message">{errors.firstName.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="lastName">Last Name:</label>
                    <input 
                        type="text" 
                        id="lastName" 
                        placeholder="Enter your last name" 
                        {...register("lastName", { onChange: () => trigger("lastName") })} 
                        className={errors.lastName && touchedFields.lastName ? 'invalid' : ''}
                    />
                    {errors.lastName && <p className="error-message">{errors.lastName.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="email">Email:</label>
                    <input 
                        type="email" 
                        id="email" 
                        placeholder="Enter your email" 
                        {...register("email", { onChange: () => trigger("email") })} 
                        className={errors.email && touchedFields.email ? 'invalid' : ''}
                    />
                    {errors.email && <p className="error-message">{errors.email.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="phone">Phone Number:</label>
                    <input 
                        type="tel" 
                        id="phone" 
                        placeholder="Enter your phone number" 
                        {...register("phoneNumber", { onChange: () => trigger("phoneNumber") })} 
                        className={errors.phoneNumber && touchedFields.phoneNumber ? 'invalid' : ''}
                    />
                    {errors.phoneNumber && <p className="error-message">{errors.phoneNumber.message}</p>}
                </div>
                <div className="form-group">
                    <label htmlFor="birthday">Birthday:</label>
                    <input 
                        type="date" 
                        id="birthday" 
                        {...register("birthDate", { onChange: () => trigger("birthDate") })} 
                        className={errors.birthDate && touchedFields.birthDate ? 'invalid' : ''}
                    />
                    {errors.birthDate && <p className="error-message">{errors.birthDate.message}</p>}
                </div>
                
                <button type="submit">Register</button>
            </form>
        </div>
    );
};

export default Register;
