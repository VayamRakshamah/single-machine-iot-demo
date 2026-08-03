import{render,screen}from'@testing-library/react';import{expect,test}from'vitest';import{KpiCard}from'./KpiCard';
test('renders an industrial KPI with unit',()=>{render(<KpiCard label="Oxygen purity" value="93.82" unit="%"/>);expect(screen.getByText('Oxygen purity')).toBeInTheDocument();expect(screen.getByText(/93.82/)).toBeInTheDocument()})
