import React from 'react';
import { render as rtlRender, fireEvent } from '@testing-library/react-native';
import OpenProcedureCard from '../OpenProcedureCard';

// MOCKS

const procedure = {
    beneficiary: {
        beneficiaryCode: '13-30222123/00',
        birthDate: '1963-08-07',
        gender: 'MASCULINO',
        id: 1,
        idNumber: 30222123,
        idType: { alias: 'DNI', name: 'Documento Nacional de Identidad', id: 1 },
        lastName: 'Sanchez',
        name: 'Rodrigo',
        relationshipType: { name: 'Titular', id: 1 },
        status: { name: 'CON COBERTURA', id: 8 },
        workIdNumber: null,
    },
    closedAt: '2019-11-29T10:24:00.352477',
    createdAt: '2019-11-29T10:22:33.069937',
    description: 'Subo documentacion de discapacidad',
    expiration: '2023-11-30',
    fileCount: 1,
    id: 20,
    messages: [{ from: 'fund1', text: 'esta todo ok', sentAt: '2019-11-29T10:23:09.645002' }],
    status: { name: 'APROBADO', id: 20 },
    type: 'DisabilityProcedure',
    _links: {
        auditLogs: { href: 'https://vem-dev.capacidad.com.ar/v1/procedures/20/audit-logs' },
        files: { href: 'https://vem-dev.capacidad.com.ar/v1/procedures/disability/20/files' },
        self: { href: 'https://vem-dev.capacidad.com.ar/v1/procedures/disability/20' },
    },
};

describe('Procedure Screen test suite', () => {
    afterEach(() => {
        jest.resetAllMocks();
    });

    it('Should navigate to ProcedureDetail on procedure press', () => {
        const navigateMock = jest.fn();
        const mockProps = {
            navigation: { navigate: navigateMock },
            getProcedures: jest.fn(),
            item: procedure,
        };

        const { debug, getByTestId, queryByTestId } = rtlRender(<OpenProcedureCard {...mockProps} />);

        fireEvent.press(getByTestId('open-procedure-card-button'));
        expect(navigateMock).toHaveBeenCalled();
    });
});
